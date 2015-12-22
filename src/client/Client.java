/**
 * Client is a simple HTTP client used to issue computation requests to our server.
 * A computation request will raise any given square matrix to an exponent.
 * 
 * It can issue 3 types of requests: 
 * 	- Request to make a computation of given difficulty (exponent) on a given input (square matrix).
 *  - Request to start recording CPU times.
 *  - Request to stop recording CPU times and return the average load.
 * 
 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package client;

import java.net.*;
import lib.Lib;
import java.io.*;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Client {
	static String URL     		= "http://localhost:3000"; // Default Argument
	static int DIFFICULTY  		= 1;
	static String INPUT_FILE 	= "input.txt";
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws Exception {
		// Get program arguments.
		URL 		= (args.length > 0) ? args[0] : URL;
		DIFFICULTY 	= (args.length > 1) ? Integer.parseInt(args[1]) : DIFFICULTY;
		INPUT_FILE 	= (args.length > 2) ? args[2] : INPUT_FILE;
		
		Client client = new Client(URL);
		ComputationResult result = client.issueComputationRequest(Lib.stringFromFile(INPUT_FILE), DIFFICULTY);
		System.out.println(result);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public Client() { this(URL); }
	public Client(String _url) { URL = _url; }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public ComputationResult issueComputationRequest(String input, int difficulty) throws Exception {
		// Build the body of the request.
		String body = buildRequestBody(input, difficulty);
		
		// Get the bytes to transfer.
		byte[] postData = body.getBytes();
		
		// Start recording processing time.
		long startTime = System.currentTimeMillis();
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/compute");
		con.setDoOutput(true);
		con.setDoInput(true);
		
		// Send data.
		con.setRequestProperty("Content-Length", Integer.toString(postData.length));
		new DataOutputStream(con.getOutputStream()).write(postData);
		
		// Read response.
		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		int computationTime = Integer.parseInt(reader.readLine());
		String result = "";
		for (String line = reader.readLine(); line != null; line = reader.readLine())
			result+=line;
		reader.close();
		
		// Close connection
		con.disconnect();
		
		// Stop timer.
		long timeElapsed = System.currentTimeMillis() - startTime;
		long networkTime = timeElapsed - computationTime;
			
		// Return computation result
		return new ComputationResult(difficulty, result, networkTime, computationTime);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public void issueStartRecordingRequest() throws Exception {
		System.out.println("Issuing start_recording request.");
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/start_recording");
		
		// Get Response Code.
		int code = con.getResponseCode();
		if (code!=200) 
			throw new Exception("Did not start recording");
		
		con.disconnect();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public RecordingResult issueStopRecordingRequest() throws Exception {
		System.out.println("Issuing stop_recording request.");
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/stop_recording");	
		con.setDoInput(true);
		
		// Read response.
		BufferedReader reader 	= new BufferedReader(new InputStreamReader(con.getInputStream()));
		float cpusUsage 		= Float.parseFloat(reader.readLine());
		float cacheHitRate		= Float.parseFloat(reader.readLine());
		
		con.disconnect();
		return new RecordingResult(cpusUsage, cacheHitRate);
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public void issueSetThreadsCountRequest(int threadsCount) throws Exception {
		System.out.println("Issuing set_threads_count request.");
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/set_threads_count");	
		con.setDoOutput(true);
		
		// Get the bytes to transfer.
		String body = Integer.toString(threadsCount);
		byte[] postData = body.getBytes();
				
		// Send data.
		con.setRequestProperty("Content-Length", Integer.toString(postData.length));
		new DataOutputStream(con.getOutputStream()).write(postData);
		
		// Get Response Code.
		int code = con.getResponseCode();
		if (code!=200) 
			throw new Exception("Did not set threads count.");
		
		Thread.sleep(1000);
		con.disconnect();
	}	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public void issueEnableCachingRequest() throws Exception {
		System.out.println("Issuing enable_caching request.");
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/enable_caching");
		int code = con.getResponseCode();
		if (code!=200) 
			throw new Exception("Did not enable caching");
		con.disconnect();
	}	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public void issueDisableCachingRequest() throws Exception {
		System.out.println("Issuing disable_caching request.");
		
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/disable_caching");
		int code = con.getResponseCode();
		if (code!=200) 
			throw new Exception("Did not disable caching.");
		con.disconnect();
	}	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Build request body as a string.
	private String buildRequestBody(String body, int difficulty) {
		return Integer.toString(difficulty)+'\n'+body;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Open the HTTP connection for a POST request.
	private HttpURLConnection getConnection(String _url) throws Exception {
		URL url = new URL(_url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		return con;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}
