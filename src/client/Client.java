package client;

import java.net.*;

import lib.Lib;

import java.io.*;

public class Client {
	static String URL     		= "http://localhost:3000";
	static int DIFFICULTY  		= 1;
	static String INPUT_FILE 	= "input.txt";
	
	public static void main(String[] args) throws Exception {
		// Get program arguments.
		URL 		= (args.length > 0) ? args[0] : URL;
		DIFFICULTY 	= (args.length > 1) ? Integer.parseInt(args[1]) : DIFFICULTY;
		INPUT_FILE 	= (args.length > 2) ? args[2] : INPUT_FILE;
		
		Client client = new Client(URL);
		ComputationResult result = client.issueComputationRequest(Lib.stringFromFile(INPUT_FILE), DIFFICULTY);
		System.out.println(result);
	}

	public Client() { this(URL); }
	public Client(String _url) {
		URL = _url;
	}
	
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
		
		// Stop timer.
		long timeElapsed = System.currentTimeMillis() - startTime;
		long networkTime = timeElapsed - computationTime;
			
		// Return computation result
		return new ComputationResult(difficulty, result, networkTime, computationTime);
	}
	
	public void issueStartRecordingRequest() throws Exception {
		// Open HTTP connection.
		System.out.println("Issuing new recording");
		HttpURLConnection con = getConnection(URL + "/start_recording");
		int code = con.getResponseCode();
		if (code!=200) 
			throw new Exception("Did not start recording");
		con.disconnect();
	}
	
	public RecordingResult issueStopRecordingRequest() throws Exception {
		// Open HTTP connection.
		HttpURLConnection con = getConnection(URL + "/stop_recording");	
		con.setDoInput(true);
		
		// Read response.
		BufferedReader reader 	= new BufferedReader(new InputStreamReader(con.getInputStream()));
		float cpusUsage 		= Float.parseFloat(reader.readLine());
		float networkUsage 		= Float.parseFloat(reader.readLine());		
		
		con.disconnect();
		return new RecordingResult(cpusUsage, networkUsage);
	}
		
	// Build request body as a string.
	public String buildRequestBody(String body, int difficulty) {
		return Integer.toString(difficulty)+'\n'+body;
	}
	
	// Open the HTTP connection for a POST request.
	public HttpURLConnection getConnection(String _url) throws Exception {
		URL url = new URL(_url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		return con;
	}
}
