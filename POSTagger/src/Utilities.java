import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Utilities {

	// Read every file between two indices and add the words and their counts to a hashmap
	public static HashMap<String, Integer> ReadFilesFromTo(HashMap<String, Integer> pVocabDictionary, 
			File[] pFileList, int pFrom, int pTo)
	{

		for(int i = pFrom; i < pTo; i++) {

			try {
				Scanner lScr = new Scanner(pFileList[i]);
				AddWords(pVocabDictionary, lScr);
				lScr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		return pVocabDictionary;

	}
	
	// Read every file between two indices and add the words and their counts to a list of sentences.
	public static void ReadFilesFromTo(ArrayList<String[]> pSentenceList, File[] pFileList, int pFrom, int pTo)
	{

		for(int i = pFrom; i < pTo; i++) {

			try {
				Scanner lScr = new Scanner(pFileList[i]);
				AddWords(pSentenceList, lScr);
				lScr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}
	
	// Read every file between two indices and add the words and their counts to a number of hashmaps. This utility is used for HMM creation and processing.
	public static void ReadFilesFromTo(HashMap<String, Integer> pVocabDictionary, HashMap<String, Integer> pTagsDictionary, 
			HashMap<String, Integer> pTransissionCounts, HashMap<String, Integer> pInitialCounts, ArrayList<String> pWordsList, File[] pFileList, int pFrom, int pTo)
	{

		for(int i = pFrom; i < pTo; i++) {

			try {
				Scanner lScr = new Scanner(pFileList[i]);
				AddWords(pVocabDictionary, pTagsDictionary, pTransissionCounts, pInitialCounts, pWordsList, lScr);
				lScr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}
	
	// Count the number of times a tag appears in a set of word-tag pairs.
	public static Integer CountTag(String pTag, HashMap<String, Integer> pDictionary) {
		
		int lTagCount = 0;
		String[] lCurrentPair = new String[2];
		
		for(Map.Entry<String, Integer> lEntry : pDictionary.entrySet()) {
			
			lCurrentPair = lEntry.getKey().split(" ");
			
			if(pTag.compareTo(lCurrentPair[1]) == 0) {
				lTagCount += lEntry.getValue();
			}
		}
		
		return lTagCount;
	}
	
	// Create a list of all the unique words that appear in a dictionary containing both words
	// and tags.
	public static ArrayList<String> CreateWordsList(HashMap<String, Integer> pDictionary) {
		
		ArrayList<String> lWordsList = new ArrayList<String>();
		
		for(Map.Entry<String, Integer> lEntry : pDictionary.entrySet()) {
			
			String[] lWordTagPair = lEntry.getKey().split(" ");
			
			if(!lWordsList.contains(lWordTagPair[0])) {
				
				lWordsList.add(lWordTagPair[0]);
				
			}
			
		}
		
		return lWordsList;
		
	}
	
	// Loop through a dictionary of which the values are integers and return the sum of all
	// the integers.
	public static Integer GetCountingDictionaryTotal(HashMap<String, Integer> pDictionary) {
		
		int lTotal = 0;
		
		for(Map.Entry<String, Integer> lEntry : pDictionary.entrySet()) {
			lTotal += lEntry.getValue();
		}
		
		return lTotal;
	}
	
	// Given an HMM and a sequence of observations, perform the Viterbi algorithm to generate a prediction of states.
	public static ArrayList<HMMState> Viterbi(ArrayList<Integer> pObservationSequence,
			                                 HashSet<HMMState> pStatesSet, Integer pSentenceCount) {
		
		double lViterbiScore = -1;
		
		// Initialize necessary parameters.
		ArrayList<HashMap<HMMState, HMMState>> lViterbiGraph = new ArrayList<HashMap<HMMState, HMMState>>();
		HashMap<HMMState, Double> lPreviousStateScores = Initialize(pStatesSet, pObservationSequence.get(0));
		HashMap<HMMState, Double> lUpdatedPreviousStateScores = new HashMap<HMMState, Double>();
		
		// Keep track of the scores from the next previous states independently of the master set of previous state scores.
		lUpdatedPreviousStateScores.putAll(lPreviousStateScores);
				
		// Loop through the observations and generate a prediction for each one.
		for(int i = 1; i < pObservationSequence.size(); i++) {
			
			lViterbiGraph.add(new HashMap<HMMState, HMMState>());
			
			// Loop through the state in the HMM to get the maximum Viterbi score.
			for(HMMState lState : pStatesSet) {
								
				lViterbiScore = GetCurrentStateViterbiScore(lViterbiGraph, pStatesSet, lState, 
						lPreviousStateScores, pObservationSequence.get(i), i-1);
								
				lUpdatedPreviousStateScores.put(lState, lViterbiScore);
				
			} // Loop through states
			
			lPreviousStateScores.putAll(lUpdatedPreviousStateScores);
						
		} // Loop through observations
		
		ArrayList<HMMState> lOptimalPath = WalkViterbiGraph(lViterbiGraph, lPreviousStateScores, pSentenceCount);

		return lOptimalPath;
	}
	
/*-------------------------------------------------------*/
	// Count the words from an open file scanner and add them
	// to a hashmap
	private static void AddWords(HashMap<String, Integer> pVocabDictionary, Scanner pScr) {
				
		String lCurrentWord = new String();
		
		while(pScr.hasNext()) {
			
			lCurrentWord = pScr.next();
			
			if(!pVocabDictionary.containsKey(lCurrentWord)) {
				pVocabDictionary.put(lCurrentWord, 1);
			}
			else {
				Integer lCurrentWordCount = pVocabDictionary.get(lCurrentWord);
				pVocabDictionary.replace(lCurrentWord, ++lCurrentWordCount);
			}
			
		}			
	}
	
	// Count the words from an open file scanner and add them
	// to a list of sentences
	private static void AddWords(ArrayList<String[]> pSentenceList, Scanner pScr) {
				
		String lCurrentString = new String();
		
		while(pScr.hasNextLine()) {
			
			lCurrentString = pScr.nextLine().trim().toLowerCase();
			
			String[] lCurrentSentence = lCurrentString.split("\\s+");
			pSentenceList.add(lCurrentSentence);
			
		}			
	}
	
	// Add words to a set of hashmaps used for HMM creation and processing.
	private static void AddWords(HashMap<String, Integer> pVocabDictionary, HashMap<String, Integer> pTagsDictionary, 
			HashMap<String, Integer> pTransissionCounts, HashMap<String, Integer> pInitialCounts, ArrayList<String> pWordsList, Scanner pScr) {
		 
		String lCurrentWord = new String();
		String lCurrentTag = new String();
		String lPreviousTag = null;
		
		// Create a map of every word-tag combo for each word that is still
		// below the unka threshold and their counts.
		HashMap<String, Integer> lUnkaPairHolder = new HashMap<String, Integer>();
		
		// Create a map of words that still are below the unka threshold and their counts.
		HashMap<String, Integer> lUnkaCounter = new HashMap<String, Integer>();
				
		int i = 0;
		
		// Read the file line-by-line to facilitate determining initial probabilities.
		while(pScr.hasNextLine()) {
			
			String[] lCurrentLine = pScr.nextLine().trim().split("\\s+");
			
			// Loop through words and tags in the current line.
			for(int j = 0; j < lCurrentLine.length; j++) {
								
				lCurrentWord = lCurrentLine[j].toLowerCase();
				
				lCurrentTag = lCurrentLine[++j];
								
				lPreviousTag = UpdateMapsAndLists(lCurrentWord, lCurrentTag, lPreviousTag, lUnkaCounter, 
						lUnkaPairHolder, pVocabDictionary, pTagsDictionary, pTransissionCounts, pInitialCounts, pWordsList, j);
				
			}
			
		}
		
		// Add unka to the words list so it will be validat during testing.
		pWordsList.add("unka");
				
	}
	
	// Perform updates to every map and list needed for HMM processing.
	private static String UpdateMapsAndLists(String pCurrentWord, String pCurrentTag, String pPreviousTag, HashMap<String, Integer> pUnkaCounter, 
			HashMap<String, Integer> pUnkaPairHolder, HashMap<String, Integer> pVocabDictionary, HashMap<String, Integer> pTagsDictionary, 
			HashMap<String, Integer> pTransitionCounts, HashMap<String, Integer> pInitialCounts, ArrayList<String> pWordsList, Integer pIndex) {
		
		// If a word has never been seen, add it to the unka counter and unka pair holder.
		if(!pUnkaCounter.containsKey(pCurrentWord)) {
			pUnkaCounter.put(pCurrentWord, 1);
			pUnkaPairHolder.put(pCurrentWord + " " + pCurrentTag, 1);
			
			// If the word-tag combo has never been seen, add it to the unka pair holder. 
			// If it has, increment the count.
			if(!pVocabDictionary.containsKey("unka " + pCurrentTag)) {
				pVocabDictionary.put("unka " + pCurrentTag, 1);
			}
			else {
				Integer lCurrentCount = pVocabDictionary.get("unka " + pCurrentTag);
				pVocabDictionary.put("unka " + pCurrentTag, ++lCurrentCount);
			}
		}
		
		// If the word has been seen by the unka counter, but hasn't 
		// reached the unka threshold yet, increment the unka counter
		// entry and update the unka pairs.
		else if(pUnkaCounter.get(pCurrentWord) < 4) {
			Integer lCurrentCount = pUnkaCounter.get(pCurrentWord);
			pUnkaCounter.replace(pCurrentWord, ++lCurrentCount);

			if(!pVocabDictionary.containsKey("unka " + pCurrentTag)) {
				pVocabDictionary.put("unka " + pCurrentTag, 1);
			}
			else {
				lCurrentCount = pVocabDictionary.get("unka " + pCurrentTag);
				pVocabDictionary.put("unka " + pCurrentTag, ++lCurrentCount);
			}

			// If the word-tag pair hasn't been seen add it to the unka pair holder.
			// Otherwise, increment its count.
			if(!pUnkaPairHolder.containsKey(pCurrentWord + " " + pCurrentTag)) {
				pUnkaPairHolder.put(pCurrentWord + " " + pCurrentTag, 1);
			}
			else {
				lCurrentCount = pUnkaPairHolder.get(pCurrentWord + " " + pCurrentTag);
				pUnkaPairHolder.replace(pCurrentWord + " " + pCurrentTag, ++lCurrentCount);
			}
		}
		
		// If the number of times a word has been seen reaches the unka threshold, remove
		// the unka pairs for that word from the master word list and add the 
		// actual word-tag pairs.
		else if(pUnkaCounter.get(pCurrentWord) == 4) {
			Integer lCurrentCount = pUnkaCounter.get(pCurrentWord);
			pUnkaCounter.replace(pCurrentWord, ++lCurrentCount);
			HashMap<String, Integer> lNotUnkaPairs = FindNotUnkaPairs(pCurrentWord, pUnkaPairHolder, pVocabDictionary);
			pVocabDictionary.putAll(lNotUnkaPairs);
			pWordsList.add(pCurrentWord);
		}
		else { // The word has exceeded the unka threshold

			pCurrentWord = pCurrentWord + " " + pCurrentTag;

			if(!pVocabDictionary.containsKey(pCurrentWord)) {
				pVocabDictionary.put(pCurrentWord, 1);
			}
			else {
				Integer lCurrentWordCount = pVocabDictionary.get(pCurrentWord);
				pVocabDictionary.put(pCurrentWord, ++lCurrentWordCount);
			}

		}

		if(!pTagsDictionary.containsKey(pCurrentTag)) {
			pTagsDictionary.put(pCurrentTag, 1);
		}
		else {
			Integer lCurrentTagCount = pTagsDictionary.get(pCurrentTag);
			pTagsDictionary.replace(pCurrentTag, ++lCurrentTagCount);
		}			

		// Record the previous tag (unless it is the first tag, in which
		// case it is null) in order to keep track of every state-to-state
		// transition that occurs and how many times it occurs.
		if(pPreviousTag == null) {
			pPreviousTag = pCurrentTag;
		}
		else {
			String lTagPair = pPreviousTag + " " + pCurrentTag;
			if(!pTransitionCounts.containsKey(lTagPair)) {
				pTransitionCounts.put(lTagPair, 1);
			}
			else {
				Integer lCurrentTransissionCount = pTransitionCounts.get(lTagPair);
				pTransitionCounts.replace(lTagPair, ++lCurrentTransissionCount);
			}
			pPreviousTag = pCurrentTag;
		}
		
		// If this is the first tag in the sentence, update the initial count for the tag.
		if(pIndex == 1) {
			if(!pInitialCounts.containsKey(pCurrentTag)) {
				pInitialCounts.put(pCurrentTag, 1);
			}
			else {
				Integer lCurrentInitialCount = pInitialCounts.get(pCurrentTag);
				pInitialCounts.replace(pCurrentTag, ++lCurrentInitialCount);
			}
		}
		
		return pPreviousTag;
		
	}
	
	private static HashMap<String, Integer> FindNotUnkaPairs(String pWord, HashMap<String, Integer> lCurrentUnkaPairs, HashMap<String, Integer> pVocabDictionary) {
		
		HashMap<String, Integer> lNotUnkaPairs = new HashMap<String, Integer>();
		
		for(Map.Entry<String, Integer> lEntry : lCurrentUnkaPairs.entrySet()) {
		 
			String lWordTag[] = lEntry.getKey().split(" ");
			String lWord = lWordTag[0];
			
			if(lWord.compareTo(pWord) == 0) {
				if(!lNotUnkaPairs.containsKey(lEntry.getKey())) {
					lNotUnkaPairs.put(lEntry.getKey(), 1);
				}
				else {
					Integer lCurrentCount = lNotUnkaPairs.get(lEntry.getKey());
					lNotUnkaPairs.replace(lEntry.getKey(), ++lCurrentCount); 
				}
				pVocabDictionary.remove(lEntry.getKey());
			}
		}
		
		return lNotUnkaPairs;
	}
	
	private static HashMap<HMMState, Double> Initialize(HashSet<HMMState> pStatesSet,
			Integer pObservation) {
		
		HashMap<HMMState, Double> lInitialStateScores = new HashMap<HMMState, Double>();
		
		for(HMMState lState : pStatesSet) {
			
			lInitialStateScores.put(lState, 
					Math.log(lState.mInitialProbability) + Math.log(lState.mEmissionProbabilities[pObservation]));
			
		}
		
		return lInitialStateScores;
	}
	
	private static double GetCurrentStateViterbiScore(ArrayList<HashMap<HMMState, HMMState>> pViterbiGraph,
			HashSet<HMMState> pStatesSet, HMMState pCurrentState, HashMap<HMMState, Double> pPreviousStateScores,
			Integer pCurrentObservation, Integer pCurrentObservationIndex) {
		
		HMMState lMaxState = null;
		double lCurrentScore = 0;
		double lMaxScore = 0;
		
		for(HMMState lSourceState : pStatesSet) {
			
			lCurrentScore = pPreviousStateScores.get(lSourceState) + 
					Math.log(lSourceState.mTransitionProbabilities.get(pCurrentState));

			if(lMaxState == null || lCurrentScore > lMaxScore) {
				lMaxState = lSourceState;
				lMaxScore = lCurrentScore;
			}

		}
		
		pViterbiGraph.get(pCurrentObservationIndex).put(pCurrentState, lMaxState); 
		
		return (lMaxScore + Math.log(pCurrentState.mEmissionProbabilities[pCurrentObservation]));
		
	}
	
	private static ArrayList<HMMState> WalkViterbiGraph(ArrayList<HashMap<HMMState, HMMState>> pViterbiGraph, 
			HashMap<HMMState, Double> pFinalScores, Integer pSentenceCount) {
		
		HMMState lMaxState = null;
		double lMaxValue = 0;
		
		ArrayList<HMMState> lOptimalPath = new ArrayList<HMMState>();
		
		for(Map.Entry<HMMState, Double> lEntry : pFinalScores.entrySet()) {
			
			if(lMaxState == null || lEntry.getValue() > lMaxValue) {
				
				lMaxValue = lEntry.getValue();
				lMaxState = lEntry.getKey();
			}
			
		}
		
		lOptimalPath.add(lMaxState);
		
		int i = pViterbiGraph.size() - 1;
		
		while(i >= 0) {
			lMaxState = pViterbiGraph.get(i--).get(lMaxState);
			lOptimalPath.add(lMaxState);
		}
		
		Collections.reverse(lOptimalPath);

//		REPORTING CODE
//
//		if(pSentenceCount < 20) {
//			
//			System.out.println("For sentence " + (pSentenceCount + 1) + ", Sequence: " );
//			for(HMMState lState : lOptimalPath) {
//				System.out.print(lState.GetStateName() + " ");
//			}
//			System.out.print(lMaxValue + "\n");
//			
//		}
		
		return lOptimalPath;
		
	}
	
}
