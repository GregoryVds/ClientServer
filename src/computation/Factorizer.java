package computation;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class Factorizer {
	public static List<String> factorize(BigInteger number) {
		List<String> factors = new LinkedList<String>();
		BigInteger two  = BigInteger.valueOf(2);
		BigInteger one  = BigInteger.ONE;
		BigInteger zero = BigInteger.ZERO;
		
		for (BigInteger divisor = two; number.compareTo(one) > 0; divisor = divisor.add(one)) {
			for (; number.remainder(divisor).equals(zero); number = number.divide(divisor)) {
				factors.add(divisor.toString());
			}
		}
		
		return factors;
	}
}