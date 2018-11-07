package TextTransformation;
// See https://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html 


import org.json.*;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;


//TODO: Document class definition
public class SimpleHttpServer {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(Constants.Networking.socketAddress, 0);
		server.createContext("/", new InfoHandler());
		server.createContext("/transform", new NetworkHandler());
		server.createContext("/forwardedtransform", new ForwardHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	static class InfoHandler implements HttpHandler {
		public void handle(HttpExchange e) throws IOException {
			String response = "Hello! Welcome to the Indigo-O Text Transformer!\n";
			response += "\tTry /transform to transform HTML";
			e.sendResponseHeaders(200, response.length());
			OutputStream os = e.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	static class NetworkHandler implements HttpHandler {
		static private HtmlParser parser = new HtmlParser();
		public void handle(HttpExchange e) throws IOException {

			Headers h = e.getResponseHeaders();
			JSONObject obj = new JSONObject(e.getRequestBody());
			String response = "Uh oh! Something went wrong...";
			
			try {
				OutputDataStructure out = NetworkHandler.parser.parse(obj);
				response = (String)out.toString();
				e.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
			} catch (Exception err) {
				response = err.getStackTrace().toString();
				e.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length());
			}

			OutputStream os = e.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	static class ForwardHandler implements HttpHandler {
		
		private static HttpURLConnection makeConnection(String sURL) throws Exception {
			URL url = new URL(sURL);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("POST");
			return c;
		}
		
		private void send(String s, URL to) throws Exception {
			HttpURLConnection c = (HttpURLConnection) to.openConnection();
			// TODO: Configure c
			c.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(c.getOutputStream());
			out.writeBytes(s);
			out.flush();
			out.close();
		}
		
		public void handle(HttpExchange e) throws IOException {
			Headers h = e.getResponseHeaders();
			JSONObject obj = new JSONObject(e.getRequestBody());

			String response = "Uh oh! Something went wrong...";
			try {
				OutputDataStructure out = NetworkHandler.parser.parse(obj);
				
				URL linkAddress = new URL(obj.getString(Constants.JSONKeys.linkForwardAddress));
				send(out.linksToString(), linkAddress);
				
				URL indexAddress = new URL(obj.getString(Constants.JSONKeys.indexingForwardAddress));
				send(out.ngramToString(), indexAddress);
				
				e.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
			} catch (Exception err) {
				response = err.getStackTrace().toString();
				e.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length());
			}

			OutputStream os = e.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
