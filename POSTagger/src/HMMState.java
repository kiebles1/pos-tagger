import java.util.HashMap;

public class HMMState {
	
	public HashMap<HMMState, Double> mTransitionProbabilities;
	public Double[] mEmissionProbabilities;
	public Double mInitialProbability;
	public String mStateName;
	
	public HMMState(Double[] pEmissionProbabilities,
			Double pInitialProbability) {
		
		mEmissionProbabilities = pEmissionProbabilities;
		mInitialProbability = pInitialProbability;
	}
	
	
	public HMMState(Double[] pEmissionProbabilities,
			Double pInitialProbability,
			String pStateName) {
		
		mEmissionProbabilities = pEmissionProbabilities;
		mInitialProbability = pInitialProbability;
		mStateName = pStateName;
	}
	
	public void SetTransitionProbabilities(HashMap<HMMState, Double> pTransitionProbabilities) {
		
		mTransitionProbabilities = pTransitionProbabilities;
	}
	
	public String GetStateName()
	{
		
		if(mStateName != null)
		{
			return mStateName;
		}
		else
		{
			return "NO NAME SET";
		}
	}
	
}
