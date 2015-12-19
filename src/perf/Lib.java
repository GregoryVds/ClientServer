package perf;

public class Lib {

	static String convertStreamToString(java.io.InputStream inputStream) {
		// Convert a stream object to a string object.
		java.util.Scanner s = new java.util.Scanner(inputStream);
		s.useDelimiter("\\A");
	    String str = s.hasNext() ? s.next() : ""; 
	    s.close();
	    return str;
	}
}
