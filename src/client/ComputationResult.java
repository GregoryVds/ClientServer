package client;

public class ComputationResult {
	private long networkTime;
	private long computationTime;
	private String result;
	private int difficulty;
	private float cpusUsage;
	
	ComputationResult(int _difficulty, String _result, long _networkTime, long _computationTime, float _cpusUsage) {
		difficulty		= _difficulty;
		networkTime 	= _networkTime;
		computationTime = _computationTime;
		result 			= _result;
		cpusUsage		= _cpusUsage;
	}
	
	public String toString() {
		String str = "COMPUTATION RESULT:\n";
		str+= String.format("Difficulty: %d.\n", getDifficulty());
		str+= String.format("Total time: %d ms.\n", getTotalTime());
		str+= String.format("Computation time: %d ms.\n", getComputationTime());
		str+= String.format("Network time: %d ms.\n", getNetworkTime());
		str+= String.format("CPUs usage: %d ms.\n", getCpusUsage());
		return str;
	}
	
	public long getNetworkTime() { return networkTime; }
	public long getComputationTime() { return computationTime; }
	public String getResult() { return result; }
	public long getTotalTime() { return networkTime+computationTime; }
	public int getDifficulty() { return difficulty; }
	public float getCpusUsage() { return cpusUsage; }
}