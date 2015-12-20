package lib;
import java.io.*;

public class Lib {
	
	// Returns input as a String from input file
	public static String stringFromFile(String filePath) throws IOException {
		FileInputStream inputStream = new FileInputStream(filePath);
	    String input = Lib.convertStreamToString(inputStream);
	    inputStream.close();
	    return input;
	}

	// Convert a stream object to a string object.
	static String convertStreamToString(java.io.InputStream inputStream) {
		java.util.Scanner s = new java.util.Scanner(inputStream);
		s.useDelimiter("\\A");
	    String str = s.hasNext() ? s.next() : ""; 
	    s.close();
	    return str;
	}
}
