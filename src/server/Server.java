/**
 * Server acts as a simple HTTP server, responding to 3 types of requests:
 * 		- A POST request "/compute" will raise a given matrix to a given exponent.
 * 		- A request on "/start_recording" asks the server to start recording the CPU utilization.
 * 		- A request on "/stop_recording" asks the server to stop recording the CPU utilization 
 * 		  and return the average load.
 * 
 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package server;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.io.*;
import com.sun.net.httpserver.*;
import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import java.util.HashMap;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Server {
	static int PORT_NUMBER 		 = 3000; 	// Default Argument
	static int THREADS_COUNT 	 = 1;		// Default Argument
	static int DELAY_MS     	 = 500;		// Default Argument
	static boolean CACHE_ANSWERS = false;	// Default Argument
	static int SOCKET_BACKLOG	 = 1000;	// Default Argument
	
	static HashMap<String, String> cache;
	static CpuTimes startCpus;
	static JavaSysMon monitor;
	static HttpServer server;
	
	static int cacheHits   = 0;
	static int cacheMisses = 0;
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws IOException {
		// Get program arguments.
		PORT_NUMBER 	= (args.length > 0) ? Integer.parseInt(args[0]) : PORT_NUMBER;
		THREADS_COUNT 	= (args.length > 1) ? Integer.parseInt(args[1]) : THREADS_COUNT;
		DELAY_MS 		= (args.length > 2) ? Integer.parseInt(args[2]) : DELAY_MS;
		CACHE_ANSWERS	= (args.length > 3) ? Boolean.parseBoolean(args[3]) : CACHE_ANSWERS;
		SOCKET_BACKLOG	= (args.length > 4) ? Integer.parseInt(args[4]) : SOCKET_BACKLOG;
	
		// Initialize cache
		cache = new HashMap<String, String>();
		
		// Kick-off web-server of specified port with a specified thread pool.
		startServer();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static void startServer() {
		try {
			// Init server
			initServer();
			
			// Use a thread pools for multithreading.
			server.setExecutor(Executors.newFixedThreadPool(THREADS_COUNT)); 
			
			// Kick-off web-server.
			server.start();
			System.out.println("Server started.");
			System.out.format("Listening on port: %d.\n", PORT_NUMBER);
			System.out.format("Fake delay: %d.\n", DELAY_MS);
			System.out.format("Cache answers: %b.\n", CACHE_ANSWERS);
			
		} catch (Exception e) {
			System.out.println("Failed to start web server");
			e.printStackTrace();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static void initServer() throws IOException {
		// Initialize web server on specified port number.
		server = HttpServer.create(new InetSocketAddress(PORT_NUMBER), SOCKET_BACKLOG);
		
		// Set request handlers and name space.
		server.createContext("/compute", 			new computationHandler());
		server.createContext("/start_recording", 	new startRecordingHandler());
		server.createContext("/stop_recording", 	new stopRecordingHandler());
		server.createContext("/enable_caching", 	new enableCachingHandler());
		server.createContext("/disable_caching", 	new disableCachingHandler());
		server.createContext("/set_threads_count", 	new setThreadsCountHandler());
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized void initCpusRecording() {
		System.out.println("Reinit CPUS recording");
		monitor = new JavaSysMon();
		startCpus = monitor.cpuTimes();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized float getCpusUsage() {
		return monitor.cpuTimes().getCpuUsage(startCpus);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	static synchronized String getFromCache(String key) { return cache.get(key); }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized void addToCache(String key, String result) { cache.put(key, result); }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized void setCacheAnswers(boolean bool) { CACHE_ANSWERS = bool; }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized void clearCache() { 
		cache.clear();
		cacheHits=0;
		cacheMisses=0;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	static synchronized void newCacheHit() 		{ cacheHits++; }
	static synchronized void newCacheMiss() 	{ cacheMisses++; }
	static synchronized float getHitRate() 		{ return ((float)cacheHits)/(cacheMisses+cacheHits); }
		
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class computationHandler implements HttpHandler {
		        
		public String compute(String input, int difficulty) {
			Computation comp = new Computation(input, difficulty, DELAY_MS);
			if (CACHE_ANSWERS) {
				// Get cached answer. 
				String cachedAnswer = getFromCache(comp.cacheKey());
				
				// If answer is cached, return it immediately.
				if (cachedAnswer!=null) {
					newCacheHit();
					return cachedAnswer;
				}
				// Else compute it.
				else { 
					newCacheMiss();
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
				long startTime = System.currentTimeMillis();
				
				// Read request difficulty and data.
				BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
				int difficulty = Integer.parseInt(reader.readLine());
				String input = "";
				for (String line = reader.readLine(); line != null; line = reader.readLine())
					input+=line;
				reader.close();
				
				// Print thread debug information.
				String threadName = Thread.currentThread().getName();
				System.out.format("Request processed by %s. Exponent: %d. Input: %s\n", threadName, difficulty, input);
				
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
			} catch (Exception e) { e.printStackTrace(); }	
       }
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class startRecordingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Start Recording Request");
			clearCache();
			initCpusRecording();
			exchange.sendResponseHeaders(200, (long)0);
			exchange.getResponseBody().close();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class stopRecordingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Stop Recording Request");
			// Get data
			float cpusUsage = getCpusUsage();
			float cacheHitRate = getHitRate();
		
			// Prepare response
			String response = cpusUsage + "\n" + cacheHitRate + "\n";
			
			// Send response
			exchange.sendResponseHeaders(200, response.length());
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(response.getBytes());
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
		
	public static class enableCachingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Enable Caching.");
			setCacheAnswers(true);
			exchange.sendResponseHeaders(200, (long)0);
			exchange.getResponseBody().close();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class disableCachingHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("Disable Caching.");
			setCacheAnswers(false);
			clearCache();
			exchange.sendResponseHeaders(200, (long)0);
			exchange.getResponseBody().close();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
			
	public static class setThreadsCountHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {		
			// Read request difficulty and data.
			BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
			int threadsCount = Integer.parseInt(reader.readLine());
			System.out.format("Set threads count to %d\n", threadsCount);
			reader.close();
			
			// Respond
			exchange.sendResponseHeaders(200, (long)0);
			exchange.getResponseBody().close();
			
			// Restart server
			server.stop(0);
			System.out.println("Server stopped.");
			initServer();
			System.out.println("Reinit with ."+threadsCount);
			server.setExecutor(Executors.newFixedThreadPool(threadsCount));
			System.out.println("Reset threadsCOunt");
			server.start();
			System.out.println("Restarted");
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}