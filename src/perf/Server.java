package perf;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.*;
import com.sun.net.httpserver.*;
import java.math.BigInteger;
import java.util.List;

public class Server {
	static int PORT_NUMBER = 3001;
			
	public static void main(String[] args) throws IOException {
		HttpServer myServer = HttpServer.create(new InetSocketAddress(PORT_NUMBER), 0);
		myServer.createContext("/factorize", new requestHandler());
		myServer.setExecutor(null); 
		myServer.start();
		System.out.printf("Server started, listening on port: %d", PORT_NUMBER);
	}
	
	static String convertStreamToString(java.io.InputStream inputStream) {
		java.util.Scanner s = new java.util.Scanner(inputStream);
		s.useDelimiter("\\A");
	    String str = s.hasNext() ? s.next() : ""; 
	    s.close();
	    return str;
	}

	public static class requestHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
           InputStream inputStream = exchange.getRequestBody();
           String body = convertStreamToString(inputStream); 
           List<String> factors = Factorizer.factorize(new BigInteger(body));
           String response = String.join(" ", factors);           
           exchange.sendResponseHeaders(200, response.length());
           OutputStream outputStream = exchange.getResponseBody();
           outputStream.write(response.getBytes());
           outputStream.close();
       }
    }
}