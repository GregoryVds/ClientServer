package client;

public class RecordingResult {
	private float cpusUsage;
	private float networkUsage;
	
	RecordingResult(float _cpusUsage, float _networkUsage) {
		cpusUsage 		= _cpusUsage;
		networkUsage 	= _networkUsage;
	}
	
	public float getCpusUsage() { return cpusUsage; }
	public float getNetworkUsage() { return networkUsage; }
}