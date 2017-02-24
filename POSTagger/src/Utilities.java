import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Utilities {
	
	public static final String FILE_PATH_START = "./POSData/"; // The path to the sentiment data is different when using
	// the Eclipse development environment

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
	
	public static ArrayList<HMMState> Viterbi(ArrayList<Integer> pObservationSequence,
			                                 HashSet<HMMState> pStatesSet) {
		
		double lViterbiScore;
		
		ArrayList<HashMap<HMMState, HMMState>> lViterbiGraph = new ArrayList<HashMap<HMMState, HMMState>>();
		HashMap<HMMState, Double> lPreviousStateScores = Initialize(pStatesSet, pObservationSequence.get(0));
		HashMap<HMMState, Double> lUpdatedPreviousStateScores = new HashMap<HMMState, Double>();
		lUpdatedPreviousStateScores.putAll(lPreviousStateScores);
		
		/*------------TEST CODE-----------*/
		
		for(Map.Entry<HMMState, Double> lEntry : lPreviousStateScores.entrySet()) {
			
			System.out.println(lEntry.getKey().GetStateName() + " has a probability of " + 
					lEntry.getValue() + " for observation # " + 0);
			
		}
		
		/*--------------------------------*/
		
		for(int i = 1; i < pObservationSequence.size(); i++) {
			
			lViterbiGraph.add(new HashMap<HMMState, HMMState>());
			
			for(HMMState lState : pStatesSet) {
								
				lViterbiScore = GetCurrentStateViterbiScore(lViterbiGraph, pStatesSet, lState, 
						lPreviousStateScores, pObservationSequence.get(i), i-1);
				
				lUpdatedPreviousStateScores.put(lState, lViterbiScore);
				
			} // Loop through states
			
			lPreviousStateScores.putAll(lUpdatedPreviousStateScores);
			
			/*------------TEST CODE-----------*/
			
			for(Map.Entry<HMMState, Double> lEntry : lPreviousStateScores.entrySet()) {
				
				System.out.println(lEntry.getKey().GetStateName() + " has a probability of " + 
						lEntry.getValue() + " for observation # " + i);
				
			}
			
			/*--------------------------------*/
			
		} // Loop through observations
		
		ArrayList<HMMState> lOptimalPath = WalkViterbiGraph(lViterbiGraph, lPreviousStateScores);
		
		/*------------TEST CODE-----------*/
		
		for(HMMState lState : lOptimalPath) {
			System.out.println(lState.GetStateName());
		}
		
		/*--------------------------------*/
		
		return lOptimalPath;
	}
	
/*-------------------------------------------------------*/
	// Count the words from an open file scanner and add them
	// to a hashmap
	private static void AddWords(HashMap<String, Integer> pVocabDictionary, Scanner pScr) {
				
		String lCurrentWord = new String();
		
		while(pScr.hasNext()) {
			
			lCurrentWord = pScr.next() + " " + pScr.next();
			
			if(!pVocabDictionary.containsKey(lCurrentWord)) {
				pVocabDictionary.put(lCurrentWord, 1);
			}
			else {
				Integer lCurrentWordCount = pVocabDictionary.get(lCurrentWord);
				pVocabDictionary.replace(lCurrentWord, ++lCurrentWordCount);
			}
		}			
	}
	
	private static Integer CountTag(String pTag, HashMap<String, Integer> pDictionary) {
		
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
	
	private static HashMap<HMMState, Double> Initialize(HashSet<HMMState> pStatesSet,
			Integer pObservation) {
		
		HashMap<HMMState, Double> lInitialStateScores = new HashMap<HMMState, Double>();
		
		for(HMMState lState : pStatesSet) {
			
			lInitialStateScores.put(lState, 
					lState.mInitialProbability * lState.mEmissionProbabilities[pObservation]);
			
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
			
			lCurrentScore = pPreviousStateScores.get(lSourceState) * 
					lSourceState.mTransitionProbabilities.get(pCurrentState);

			if(lMaxState == null || lCurrentScore > lMaxScore) {
				lMaxState = lSourceState;
				lMaxScore = lCurrentScore;
			}

		}
		
		pViterbiGraph.get(pCurrentObservationIndex).put(pCurrentState, lMaxState); 
		
		return (lMaxScore * pCurrentState.mEmissionProbabilities[pCurrentObservation]);
		
	}
	
	private static ArrayList<HMMState> WalkViterbiGraph(ArrayList<HashMap<HMMState, HMMState>> pViterbiGraph, 
			HashMap<HMMState, Double> pFinalScores) {
		
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
		
		return lOptimalPath;
		
	}
	
}
