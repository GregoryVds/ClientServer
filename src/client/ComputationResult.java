/**
 * ComputationResult is a simple class that encapsulate the result of a computation performed by
 * the server.
 * 
 * It contains the following information:
 * 	- Computation input (the square matrix).
 *  - Computation difficulty (the exponent).
 *  - Network time is ms.
 *  - Computation time in ms.
 *  
 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package client;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class ComputationResult {
	private long networkTime;
	private long computationTime;
	private String result;
	private int difficulty;
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	ComputationResult(int _difficulty, String _result, long _networkTime, long _computationTime) {
		difficulty		= _difficulty;
		networkTime 	= _networkTime;
		computationTime = _computationTime;
		result 			= _result;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public String toString() {
		String str = "COMPUTATION RESULT:\n";
		str+= String.format("Difficulty: %d.\n", getDifficulty());
		str+= String.format("Total time: %d ms.\n", getTotalTime());
		str+= String.format("Computation time: %d ms.\n", getComputationTime());
		str+= String.format("Network time: %d ms.\n", getNetworkTime());
		return str;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public long getNetworkTime() { return networkTime; }
	public long getComputationTime() { return computationTime; }
	public String getResult() { return result; }
	public long getTotalTime() { return networkTime+computationTime; }
	public int getDifficulty() { return difficulty; }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}