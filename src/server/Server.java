package server;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.io.*;
import com.sun.net.httpserver.*;
import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;

import java.util.HashMap;

public class Server {
	static int PORT_NUMBER 		 = 3000; 	// Default Argument
	static int THREADS_COUNT 	 = 1;		// Default Argument
	static int DELAY_MS     	 = 500;		// Default Argument
	static boolean CACHE_ANSWERS = false;	// Default Argument
	
	static HashMap<String, String> cache;
	
	static CpuTimes startCpus;
	static JavaSysMon monitor;
	
	public static void main(String[] args) throws IOException {
		// Get program arguments.
		PORT_NUMBER 	= (args.length > 0) ? Integer.parseInt(args[0]) : PORT_NUMBER;
		THREADS_COUNT 	= (args.length > 1) ? Integer.parseInt(args[1]) : THREADS_COUNT;
		DELAY_MS 		= (args.length > 2) ? Integer.parseInt(args[2]) : DELAY_MS;
		CACHE_ANSWERS	= (args.length > 3) ? Boolean.parseBoolean(args[3]) : CACHE_ANSWERS;
	
		// Initialize cache if needed.
		if (CACHE_ANSWERS)
			cache = new HashMap<String, String>();
		
		// Kick-off web-server of specified port with a specified thread pool.
		startServer();
	}
	
	static void startServer() {
		try {
			// Initialize web server on specified port number.
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT_NUMBER), 0);
			
			// Set request handlers and name space.
			server.createContext("/compute", new computationHandler());
			server.createContext("/start_recording", new startRecordingHandler());
			server.createContext("/stop_recording", new stopRecordingHandler());
			
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
	
	static synchronized void initNetworkRecording() {
		// TODO
	}
	
	static synchronized float getNetworkUsage() {
		// TODO
		return 0.0f;
	}
	
	static synchronized void initCpusRecording() {
		System.out.println("Reinit CPUS recording");
		monitor = new JavaSysMon();
		startCpus = monitor.cpuTimes();
	}
	
	static synchronized float getCpusUsage() {
		return monitor.cpuTimes().getCpuUsage(startCpus);
	}

	static synchronized String getFromCache(String key) {
		return cache.get(key);
	}
	
	static synchronized void addToCache(String key, String result) {
		cache.put(key, result);
	}
	
	public static class computationHandler implements HttpHandler {
		        
		public String compute(String input, int difficulty) {
			Computation comp = new Computation(input, difficulty, DELAY_MS);
			if (CACHE_ANSWERS) {
				// Get cached answer. 
				String cachedAnswer = getFromCache(comp.cacheKey());
				
				// If answer is cached, return it immediately.
				if (cachedAnswer!=null) 
					return cachedAnswer;
				// Else compute it.
				else { 
					String result = comp.compute();
					addToCache(comp.cacheKey(), result);
					return result;
				}
			}
			else 
				return comp.compute();
		}
		
		public void handle(HttpExchange exchange) {
			try {
				// Start recording processing time.
				
				
				// Read request difficulty and data.
				BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
				int difficulty = Integer.parseInt(reader.readLine());
				String input = "";
				for (String line = reader.readLine(); line != null; line = reader.readLine())
					input+=line;
				
				// Print thread debug information.
				String threadName = Thread.currentThread().getName();
				System.out.format("Request for %s (Difficulty %d) processed by %s.\n", input, difficulty, threadName);
				
				// Compute
				long startTime = System.currentTimeMillis();
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
				
			} catch (Exception e) { e.printStackTrace(); }	
       }
    }
	
	public static class startRecordingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Start Recording Request");
			initCpusRecording();
			exchange.sendResponseHeaders(200, (long)0);
			exchange.getResponseBody().close();
		}
	}
	
	public static class stopRecordingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Stop Recording Request");
			// Get data
			float cpusUsage    = getCpusUsage();
			float networkUsage = getNetworkUsage();
			
			// Prepare response
			String response = cpusUsage + "\n" + networkUsage;
			
			// Send response
			exchange.sendResponseHeaders(200, response.length());
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(response.getBytes());
		}
	}
}