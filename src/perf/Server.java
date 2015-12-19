package perf;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.io.*;
import com.sun.net.httpserver.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

public class Server {
	static int PORT_NUMBER 		 = 3000;
	static int THREADS_COUNT 	 = 1;
	static int DELAY_MS     	 = 500;	
	static boolean CACHE_ANSWERS = false;
	static HashMap<String, String> CACHE;
			
	public static void main(String[] args) throws IOException {
		// Get program arguments.
		PORT_NUMBER 	= (args.length > 0) ? Integer.parseInt(args[0]) : PORT_NUMBER;
		THREADS_COUNT 	= (args.length > 1) ? Integer.parseInt(args[1]) : THREADS_COUNT;
		DELAY_MS 		= (args.length > 2) ? Integer.parseInt(args[2]) : DELAY_MS;
		CACHE_ANSWERS	= (args.length > 3) ? Boolean.parseBoolean(args[3]) : CACHE_ANSWERS;
	
		// Initialize cache if needed.
		if (CACHE_ANSWERS)
			CACHE = new HashMap<String, String>();
		
		// Kick-off web-server of specified port with a specified thread pool.
		startServer();
	}
	
	static void startServer() {
		try {
			// Initialize web server on specified port number.
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT_NUMBER), 0);
			
			// Set request handler and name space.
			server.createContext("/compute", new requestHandler());
			
			// Use a thread pools for multithreading.
			server.setExecutor(Executors.newFixedThreadPool(THREADS_COUNT)); 
			
			// Kick-off web-server.
			server.start();
			System.out.format("Server started.\nListening on port: %d.\n", PORT_NUMBER);
		} catch (Exception e) {
			System.out.println("Failed to start web server");
			e.printStackTrace();
		}
	}

	public static class requestHandler implements HttpHandler {
		
		public String compute(String input, int difficulty) {
			// Get cached answer. 
			String cachedAnswer = CACHE_ANSWERS ? CACHE.get(input) : null;
			
			// If answer is cached, return it immediately.
			if (cachedAnswer!=null) 
				return cachedAnswer;
			// Else compute it.
			else {
				// Delegate computation.
				List<String> factors = Factorizer.factorize(new BigInteger(input));
				String result = String.join(" ", factors);
				
				// Simulate fake delay in processing.
				try {
					Thread.sleep(DELAY_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// Store result in cache.
				CACHE.put(input, result);
				
				// Return result as string.
				return result;
			}
		}
		
		public void handle(HttpExchange exchange) {
			try {
				// Start recording processing time.
				long startTime = System.currentTimeMillis();
				
				// Get request difficulty and request data.
				InputStream inputStream = exchange.getRequestBody();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
				int difficulty = Integer.parseInt(reader.readLine());
				String input = "";
				for (String line = reader.readLine(); line != null; line = reader.readLine())
					input+=line;
				
				// Print thread debug information.
				String threadName = Thread.currentThread().getName();
				System.out.format("Request for %s (Difficulty %d) processed by %s.\n", input, difficulty, threadName);
				
				// Compute
				String computationResult = compute(input, difficulty);

				// Prepare response
				long timeElapsed = System.currentTimeMillis() - startTime;
				String response = Long.toString(timeElapsed) + "\n" + computationResult;
				
				// Send response
				exchange.sendResponseHeaders(200, response.length());
				OutputStream outputStream = exchange.getResponseBody();
				outputStream.write(response.getBytes());
				
				// Close stream
				outputStream.close();
			} catch (NumberFormatException | IOException e) {e.printStackTrace();}	
       }
    }
}