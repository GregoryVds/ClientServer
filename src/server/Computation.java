/**
 * Computation encapsulates the computation job. 
 * 
 * It optionally simulates a fake delay to artificially extend the computation time. 
 * It is also responsible for building the key used to cache the results of a computation.

 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package server;

import computation.SquareMatrix;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Computation {
	private String input;
	private int difficulty;
	private int delay;
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	Computation(String _input, int _difficulty, int _delay) {
		input 		= _input;
		difficulty 	= _difficulty;
		delay 		= _delay;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public String compute() {
		// Simulate fake delay in processing.		
		if (delay>0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Perform computation f(input, difficulty)
		return SquareMatrix.powerMatrix(input, difficulty);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public String cacheKey() {
		return Integer.toString(difficulty)+input;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}