import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
//		TestTraining();
		
		HashMap<String, Integer> lPairDictionary = new HashMap<String, Integer>();
		File lTrainingFile = new File("./POSData/wsj1-18.training");
		File[] lFiles = {lTrainingFile};
		Utilities.ReadFilesFromTo(lPairDictionary, lFiles, 0, 1);
		HashSet<HMMState> lStateSet = CreateHMMFromPOSPairs(lPairDictionary);
		
		//HashSet<HMMState> lTestSet = CreateTestHMM();
		//TestInitialization(lTestSet);
	}
	
/*------------------------------------------------*/
	private static HashSet<HMMState> CreateHMMFromPOSPairs(HashMap<String, Integer> pPairDictionary) {
		
		
		
		return null;
	}
	
	private static void TestTraining() {
		
		HashMap<String, Integer> lPairDictionary = new HashMap<String, Integer>();
		File lTrainingFile = new File("./POSData/wsj1-18.training");
		File[] lFiles = {lTrainingFile};
		Utilities.ReadFilesFromTo(lPairDictionary, lFiles, 0, 1);
		
		for(Map.Entry<String, Integer> lEntry : lPairDictionary.entrySet()) {
			//System.out.println("The word and POS Tag " + lEntry.getKey() + " appears " + lEntry.getValue() + " times.");
		}
		
	}
	
	private static HashSet<HMMState> CreateTestHMM() {
		
		Double[] lMediumEmissionProbabilities = {0.5, 0.5};
		Double lInitialProbability = 0.5;
		String lStateName = "Medium";
		
		HMMState lMedium = new HMMState(lMediumEmissionProbabilities, 
				lInitialProbability, 
				lStateName);
		
		Double[] lHighEmissionProbabilities = {0.75, 0.25};
		lInitialProbability = 0.2;
		lStateName = "High";
		
		HMMState lHigh = new HMMState(lHighEmissionProbabilities, 
				lInitialProbability, 
				lStateName);
		
		Double[] lLowEmissionProbabilities = {0.25, 0.75};
		lInitialProbability = 0.3;
		lStateName = "Low";

		HMMState lLow = new HMMState(lLowEmissionProbabilities, 
				lInitialProbability, 
				lStateName);
		
		HashMap<HMMState, Double> lMediumMap = new HashMap<HMMState, Double>();
		lMediumMap.put(lMedium, 0.3);
		lMediumMap.put(lHigh, 0.4);
		lMediumMap.put(lLow, 0.3);
		lMedium.SetTransitionProbabilities(lMediumMap);
		
		HashMap<HMMState, Double> lHighMap = new HashMap<HMMState, Double>();
		lHighMap.put(lMedium, 0.4);
		lHighMap.put(lHigh, 0.5);
		lHighMap.put(lLow, 0.1);
		lHigh.SetTransitionProbabilities(lHighMap);
		
		HashMap<HMMState, Double> lLowMap = new HashMap<HMMState, Double>();
		lLowMap.put(lMedium, 0.4);
		lLowMap.put(lHigh, 0.1);
		lLowMap.put(lLow, 0.5);
		lLow.SetTransitionProbabilities(lLowMap);

		HashSet<HMMState> lSet = new HashSet<HMMState>();
		lSet.add(lMedium);
		lSet.add(lHigh);
		lSet.add(lLow);
		
		return lSet;
	}
	
	private static void TestInitialization(HashSet<HMMState> pStatesSet) {
		ArrayList<Integer> lObservationSequence = new ArrayList<Integer>();
		lObservationSequence.add(0);
		lObservationSequence.add(1);
		lObservationSequence.add(1);
		lObservationSequence.add(0);
		lObservationSequence.add(1);
		
		ArrayList<HMMState> lStateList = Utilities.Viterbi(lObservationSequence, pStatesSet);
	}

}
