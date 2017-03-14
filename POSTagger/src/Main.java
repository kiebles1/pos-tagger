import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		
		// Initialize maps used to store relevant words or tags and their counts.
		HashMap<String, Integer> lPairDictionary = new HashMap<String, Integer>();
		HashMap<String, Integer> lTagsDictionary = new HashMap<String, Integer>();
		HashMap<String, Integer> lTransitionCounts = new HashMap<String, Integer>();
		HashMap<String, Integer> lInitialCounts = new HashMap<String, Integer>();
		ArrayList<String> lWordsList = new ArrayList<String>();
		
		// Perform training on training file.
		System.out.println("Starting training.");
		File lTrainingFile = new File("/user/cse842/POSData/wsj1-18.training");
		File[] lTrainingFileArray = {lTrainingFile};
		Utilities.ReadFilesFromTo(lPairDictionary, lTagsDictionary, lTransitionCounts, lInitialCounts,  lWordsList, lTrainingFileArray, 0, 1);
		
		// Create the model from the training data.
		HashSet<HMMState> lStateSet = CreateHMMFromPOSPairs(lPairDictionary, lWordsList, lTagsDictionary, lTransitionCounts, lInitialCounts);
		System.out.println("Training Complete. Starting Testing.");
		
		// Perform testing on testing file.
		ArrayList<String[]> lTestingSentences = new ArrayList<String[]>();
		File lTestingFile = new File("/user/cse842/POSData/wsj19-21.testing");
		File[] lTestingFileArray = {lTestingFile};
		Utilities.ReadFilesFromTo(lTestingSentences, lTestingFileArray, 0, 1);
		ArrayList<HMMState> lStateSequence = new ArrayList<HMMState>();
		
		// Verify HMM with truth file.
		ArrayList<String[]> lTruthSentences = new ArrayList<String[]>();
		File lTruthFile = new File("/user/cse842/POSData/wsj19-21.truth");
		File[] lTruthFileArray = {lTruthFile};
		Utilities.ReadFilesFromTo(lTruthSentences, lTruthFileArray, 0, 1);
		
        // Initialize array to track the number of tag matches and the total number of tags checked.
		Integer[] lMatchesAndTotal = {0, 0};
		
		Integer lSentenceCount = 0;
		
		// Loop through the sentences from the testing file.
		for(int i = 0; i < lTestingSentences.size(); i++) {
			
			ArrayList<Integer> lObservationSequence = new ArrayList<Integer>();
			String[] lCurrentSentence = lTestingSentences.get(i);
			
			for(String lWord : lCurrentSentence) {
								
				// Create the sequence of integers representing observations (words in this example).
				lObservationSequence.add(lWordsList.indexOf(lWord));
				
			}
			
			// Run the viterbi algorithm with the generated HMM and the test sequence.
			// Verify the resulting state sequence.
			lStateSequence = Utilities.Viterbi(lObservationSequence, lStateSet, lSentenceCount);
			VerifyStateSequence(lStateSequence, lTruthSentences.get(i), lMatchesAndTotal);
			
			lSentenceCount++;
			
		}
		
		System.out.println((double) lMatchesAndTotal[0] / (double) lMatchesAndTotal[1]); 
		
	}
	
	
/*------------------------------------------------*/
	// Verify the predicted sequence of states with a truth model.
	private static void VerifyStateSequence(ArrayList<HMMState> pStateSequence, String[] pTruthSentence, Integer[] pMatchesAndTotal) {
				
		for(int i = 0; i < pStateSequence.size(); i++) {
			
			pMatchesAndTotal[1]++;
			
			String lStateString = pStateSequence.get(i).GetStateName().toLowerCase();
			
			// If the predicted tag matches the current tag, increment the matches element
			// of the array. Skip the words in the truth file, it is assumed that they match
			// the testing data.
			if(lStateString.compareTo(pTruthSentence[i * 2 + 1]) == 0) {
				
				pMatchesAndTotal[0]++;
				
			}
			
		}
		
	}
	
	// Create and return an HMM from the set of all the pairs of training words and their corresponding tags.
	private static HashSet<HMMState> CreateHMMFromPOSPairs(HashMap<String, Integer> pPairDictionary, 
			ArrayList<String> pWordsList, HashMap<String, Integer> pTagsDictionary, 
			HashMap<String, Integer> pTransitionCounts, HashMap<String, Integer> pInitialCounts) {
		
		HashSet<HMMState> lHMM = new HashSet<HMMState>();

		// Determine how many sentences were processed in training to get the denominator for
		// the initial probability calculation.
		Integer lNumberOfSentences = Utilities.GetCountingDictionaryTotal(pInitialCounts);
		
		// Loop through the word-tag pairs and add new states to the HMM set.
		for(Map.Entry<String, Integer> lEntry : pPairDictionary.entrySet()) {
			
			String[] lWordTagPair = lEntry.getKey().split(" ");
			
			if(!HMMContainsState(lHMM, lWordTagPair[1])) {
				
				lHMM.add(CreateHMMState(pPairDictionary, pTagsDictionary, pInitialCounts, lNumberOfSentences, lWordTagPair[1], pWordsList, pTransitionCounts));
				
			}
			
		}
		
		// Loop through the new HMM set to set the transition probabilities of each state.
		for(HMMState lState : lHMM) {
			
			HashMap<HMMState, Double> lTransitionProbabilities = GetTransitionProbabilities(lState, pTransitionCounts, pTagsDictionary, lHMM);
			lState.SetTransitionProbabilities(lTransitionProbabilities);
			
		}
		
		return lHMM;
	}
	
	// Check if an HMM set contains a given state (by name).
	private static boolean HMMContainsState(HashSet<HMMState> pHMM, String pStateName) {
		
		boolean lContains = false;
		
		for(HMMState lState : pHMM) {
			
			if(lState.GetStateName().compareTo(pStateName) == 0) {
				
				lContains = true;
				
			}
			
		}
		
		return lContains;
	}
	
	// Create a new state to go into an HMM for a given tag name.
	private static HMMState CreateHMMState(HashMap<String, Integer> pPairDictionary, HashMap<String, Integer> pTagsDictionary,
			HashMap<String, Integer> pInitialCounts, Integer pNumberOfSentences, String pTag, ArrayList<String> pWordsList,
			HashMap<String, Integer> pTransitionCounts) {
				
		Double[] lEmissionProbabilityList = new Double[pWordsList.size()];
		
		// Loop through all the words and add the current tag to the end of them.
		for(String lWord : pWordsList) {
			
			String lWordTagPair = lWord + " " + pTag;
			Integer lWordTagAppearances = pPairDictionary.get(lWordTagPair);
			
			// In case a word tag combo has never been seen, set the count to 0.
			if(lWordTagAppearances == null) {
				
				lWordTagAppearances = 0;
				
			}
			
			// Add the emission probability for the given word from the loop and the tag passed in as a parameter. 
			lEmissionProbabilityList[pWordsList.indexOf(lWord)] = (double) lWordTagAppearances / (double) pTagsDictionary.get(pTag);
						
		}
		
		// Calculate the initial probability as the number of times a tag appeared first in a sentence
		// divided by the number of sentences.
		Double lInitialProbability = 0.0;
		
		if(pInitialCounts.containsKey(pTag)) {
			lInitialProbability = (double) pInitialCounts.get(pTag) / (double) pNumberOfSentences;
		}
								
		HMMState lNewState = new HMMState(lEmissionProbabilityList, lInitialProbability, pTag);
		
		return lNewState;
	}
	
	// Determine the probability of transitioning from one state to each other state.
	private static HashMap<HMMState, Double> GetTransitionProbabilities(HMMState pSourceState, HashMap<String, Integer> pTransitionCounts, 
			HashMap<String, Integer> pTagsDictionary, HashSet<HMMState> lStateSet) {
		
		HashMap<HMMState, Double> lStateTransitionProbabilities = new HashMap<HMMState, Double>();
		
		// Set lambda (for smoothing) to a value between 0 and 1.
		double lLambda = 1.0;
	
		// Loop through every possible destination state.
		for(HMMState lDestinationState : lStateSet) {
			
			double lCurrentProbability = 0.0;
			String lCurrentStatesPair = pSourceState.GetStateName() + " " + lDestinationState.GetStateName();
			
			// If the source state has been seen before, calculate the bigram probability. Otherwise, just use the probability of the destination state.
			if(pTagsDictionary.containsKey(pSourceState.GetStateName())) {
				
				if(pTransitionCounts.containsKey(lCurrentStatesPair)) {
					lCurrentProbability = (double) pTransitionCounts.get(lCurrentStatesPair) / (double) pTagsDictionary.get(pSourceState.GetStateName());
					lCurrentProbability *= lLambda;
				}
				
				if(pTagsDictionary.containsKey(lDestinationState.GetStateName())) {
					lCurrentProbability += (1.0 - lLambda) * ((double) pTagsDictionary.get(lDestinationState.GetStateName()) / (double) Utilities.GetCountingDictionaryTotal(pTagsDictionary));
				}
				
			}
			else {
				
				if(pTagsDictionary.containsKey(lDestinationState.GetStateName())) {
					lCurrentProbability = ((double) pTagsDictionary.get(lDestinationState.GetStateName()) / (double) Utilities.GetCountingDictionaryTotal(pTagsDictionary));
				}
				
			}
			
			lStateTransitionProbabilities.put(lDestinationState, lCurrentProbability);
			
		}
		
		return lStateTransitionProbabilities;
	}
	
	
	/*--------------TESTING CODE--------------*/
//	private static void TestStateSetPopulation(HashSet<HMMState> pSet) {
//		
//		for(HMMState lState : pSet) {
//			
//			for(int j = 0; j < lState.mEmissionProbabilities.length; j++) {
//				//System.out.println("The state " + lState.GetStateName() + ", for observation " + j + ", has an emission probability of " + lState.mEmissionProbabilities[j]);
//			}
//			
//			for(Map.Entry<HMMState, Double> lEntry : lState.mTransitionProbabilities.entrySet()) {
//				System.out.println("The state " + lState.GetStateName() + " transitions to the state " + lEntry.getKey().GetStateName() + " with a probability of " + lEntry.getValue());
//			}
//			
//		}
//		
//	}
//		
//	private static void TestTraining() {
//		
//		HashMap<String, Integer> lPairDictionary = new HashMap<String, Integer>();
//		File lTrainingFile = new File("./POSData/wsj1-18.training");
//		File[] lFiles = {lTrainingFile};
//		Utilities.ReadFilesFromTo(lPairDictionary, lFiles, 0, 1);
//		
//		for(Map.Entry<String, Integer> lEntry : lPairDictionary.entrySet()) {
//			//System.out.println("The word and POS Tag " + lEntry.getKey() + " appears " + lEntry.getValue() + " times.");
//		}
//		
//	}
//	
//	private static HashSet<HMMState> CreateTestHMM() {
//		
//		Double[] lMediumEmissionProbabilities = {0.5, 0.5};
//		Double lInitialProbability = 0.5;
//		String lStateName = "Medium";
//		
//		HMMState lMedium = new HMMState(lMediumEmissionProbabilities, 
//				lInitialProbability, 
//				lStateName);
//		
//		Double[] lHighEmissionProbabilities = {0.75, 0.25};
//		lInitialProbability = 0.2;
//		lStateName = "High";
//		
//		HMMState lHigh = new HMMState(lHighEmissionProbabilities, 
//				lInitialProbability, 
//				lStateName);
//		
//		Double[] lLowEmissionProbabilities = {0.25, 0.75};
//		lInitialProbability = 0.3;
//		lStateName = "Low";
//
//		HMMState lLow = new HMMState(lLowEmissionProbabilities, 
//				lInitialProbability, 
//				lStateName);
//		
//		HashMap<HMMState, Double> lMediumMap = new HashMap<HMMState, Double>();
//		lMediumMap.put(lMedium, 0.3);
//		lMediumMap.put(lHigh, 0.4);
//		lMediumMap.put(lLow, 0.3);
//		lMedium.SetTransitionProbabilities(lMediumMap);
//		
//		HashMap<HMMState, Double> lHighMap = new HashMap<HMMState, Double>();
//		lHighMap.put(lMedium, 0.4);
//		lHighMap.put(lHigh, 0.5);
//		lHighMap.put(lLow, 0.1);
//		lHigh.SetTransitionProbabilities(lHighMap);
//		
//		HashMap<HMMState, Double> lLowMap = new HashMap<HMMState, Double>();
//		lLowMap.put(lMedium, 0.4);
//		lLowMap.put(lHigh, 0.1);
//		lLowMap.put(lLow, 0.5);
//		lLow.SetTransitionProbabilities(lLowMap);
//
//		HashSet<HMMState> lSet = new HashSet<HMMState>();
//		lSet.add(lMedium);
//		lSet.add(lHigh);
//		lSet.add(lLow);
//		
//		return lSet;
//	}
//	
//	private static void TestInitialization(HashSet<HMMState> pStatesSet) {
//		ArrayList<Integer> lObservationSequence = new ArrayList<Integer>();
//		lObservationSequence.add(0);
//		lObservationSequence.add(1);
//		lObservationSequence.add(2);
//		lObservationSequence.add(3);
//		lObservationSequence.add(4);
//		
//		ArrayList<HMMState> lStateList = Utilities.Viterbi(lObservationSequence, pStatesSet);
//		
//	}

}
