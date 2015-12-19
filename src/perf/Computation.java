package perf;

public class Computation {
	private String input;
	private int difficulty;
	private int delay;
	
	Computation(String _input, int _difficulty, int _delay) {
		input 		= _input;
		difficulty 	= _difficulty;
		delay 		= _delay;
	}
	
	public String compute() {
		// Simulate fake delay in processing.
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Perform computation f(input, difficulty)
		return "dummyResult";
	}
	
	public String cacheKey() {
		return Integer.toString(difficulty)+input;
	}
}