package perf;

import java.net.*;
import java.io.*;

public class Client {
	static String BASE_URL = "http://localhost";
	static String PORT_NUMBER = "3001";
	static String FULL_URL = BASE_URL+":"+PORT_NUMBER+"/factorize";
	
	public static void main(String[] args) throws Exception {
		 String factors = issueFactorizationRequest(15);
		 System.out.println(factors);
	}
	
	public static String issueFactorizationRequest(int numberToFact) throws Exception {
		 byte[] postData = Integer.toString(numberToFact).getBytes();
		 HttpURLConnection con = getConnection();
         con.setRequestProperty("Content-Length", Integer.toString(postData.length));
         new DataOutputStream(con.getOutputStream()).write(postData);
         BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
         String response = in.readLine();
         in.close();
         return response;
	}
	
	public static HttpURLConnection getConnection() throws Exception {
		 URL url = new URL(FULL_URL);
		 HttpURLConnection con = (HttpURLConnection) url.openConnection();
		 con.setDoOutput(true);
		 con.setDoInput(true);
         con.setRequestMethod("POST");
         return con;
	}
}
