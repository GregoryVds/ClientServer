/**
 * Lib contains simple utility functions.
 *  
 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package lib;
import java.io.*;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Lib {
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Returns input as a String from input file
	public static String stringFromFile(String filePath) throws IOException {
		FileInputStream inputStream = new FileInputStream(filePath);
	    String input = Lib.convertStreamToString(inputStream);
	    inputStream.close();
	    return input;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Convert a stream object to a string object.
	public static String convertStreamToString(java.io.InputStream inputStream) {
		java.util.Scanner s = new java.util.Scanner(inputStream);
		s.useDelimiter("\\A");
	    String str = s.hasNext() ? s.next() : ""; 
	    s.close();
	    return str;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Helper to generate a square matrix of given size.
	public static String generateSquareMatrix(int size) {
		String matrix = "";
		for (int i = 0; i<size; i++) {
			for (int j = 0; j<size; j++) {
				matrix+=j+",";
			}
			matrix = matrix.substring(0, matrix.length()-1); // Remove last ","
			matrix += ";"; // End row.
		}
		matrix = matrix.substring(0, matrix.length()-1); // Remove last ";"
		return matrix;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}
