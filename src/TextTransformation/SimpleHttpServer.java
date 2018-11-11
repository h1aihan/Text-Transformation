package TextTransformation;


// JSONObject
import org.json.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;

import java.net.URL;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

// SimpleHttpServer creates a process to handle all requests
//		It responds to requests in InfoHandler, NetworkHandler, & ForwardHandler
// See https://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html 
public class SimpleHttpServer {
	static private HtmlParser parser = new HtmlParser();
	// Creates process, starts running it
	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(Constants.Networking.socketAddress, 0);
		server.createContext(Constants.Networking.rootAddress, new InfoHandler());
		server.createContext(Constants.Networking.transformAndForward, new ForwardHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}
	
	// Returns a nice little info page if you don't know what you're doing
	static class InfoHandler implements HttpHandler {
		public void handle(HttpExchange e) throws IOException {
			String response = Constants.StaticText.NetworkWelcomeMessage;
			e.sendResponseHeaders(200, response.length());
			OutputStream os = e.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	// Handles POST Requests (forward transformed text JSON to link analysis, indexing)
	static class ForwardHandler implements HttpHandler {
		
		private static HttpURLConnection makeConnection(String sURL) throws Exception {
			URL url = new URL(sURL);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("POST");
			return c;
		}
		
		// Send the string "s" to the String URL "to"
		private void send(String s, String to) throws Exception {
			HttpURLConnection c = ForwardHandler.makeConnection(to);
			c.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(c.getOutputStream());
			out.write(s.getBytes());
			out.flush();
			out.close();
		}
		
		public void handle(HttpExchange e) throws IOException {
//			Headers h = e.getResponseHeaders();
			JSONObject jsonRequest = new JSONObject(e.getRequestBody());
			JSONObject jsonResponse = new JSONObject();
			int httpStatus = HttpURLConnection.HTTP_OK;
			OutputDataStructure out;
			
			try {
				out = .parse(jsonRequest);
				jsonResponse.put(Constants.JSON.metaDataKey, out.getMetaDataJSON());
			} catch (Exception err) {
				// Unrecoverable error-- cannot parse!
				httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
				e.sendResponseHeaders(httpStatus, 0);
				return;
			}
	
			try {					
				if (jsonRequest.has(Constants.JSON.linkForwardAddressKey)) {
					String linkAddress = jsonRequest.getString(Constants.JSON.linkForwardAddressKey);
					jsonResponse.put(Constants.JSON.linksKey, out.getLinksJSON());
					send(jsonResponse.toString(), linkAddress);
				}
			} catch (Exception err) { 
				// Recoverable error-- cannot send links
				httpStatus = HttpURLConnection.HTTP_PARTIAL;
			}
			
			try {
				if (jsonRequest.has(Constants.JSON.indexingForwardAddressKey)) {
					String indexAddress = jsonRequest.getString(Constants.JSON.indexingForwardAddressKey);
					if (jsonResponse.has(Constants.JSON.linksKey)) 
						jsonResponse.remove(Constants.JSON.linksKey);
				
					jsonResponse.put(Constants.JSON.indexingForwardAddressKey, out.getNGramJSON());
					send(jsonResponse.toString(), indexAddress);
				}
			} catch (Exception err) { 
				// If two recoverable errors--> bad request!
				httpStatus = httpStatus == HttpURLConnection.HTTP_PARTIAL ? 
											HttpURLConnection.HTTP_BAD_REQUEST : 
											HttpURLConnection.HTTP_PARTIAL;
			}
			e.sendResponseHeaders(httpStatus, 0);

			
		}
	}
}
