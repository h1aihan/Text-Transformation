package TextTransformation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.*;
import javax.servlet.http.*;

// JSONObject
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

// SimpleHttpServer creates a process to handle all requests
//		It responds to requests in InfoHandler, NetworkHandler, & ForwardHandler
// See https://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html 
public class SimpleHttpServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8021309171908246830L;
	static private HtmlParser parser = new HtmlParser();
//	private HttpServer server;
//	public void setupServer() throws Exception {
//		server = HttpServer.create(Constants.Networking.socketAddress, 0);
//		server.createContext(Constants.Networking.rootAddress, new InfoHandler());
//		server.createContext(Constants.Networking.transformAndForward, new ForwardHandler());
//		server.setExecutor(null); // creates a default executor
//		server.start();
//	}
//	public void stopServer() {
//		server.stop(0);
//	}
	public void doGet(HttpServletRequest request, 
			  HttpServletResponse response) 
			  throws ServletException, IOException {
			  
			  PrintWriter out = response.getWriter();
			  out.println(Constants.StaticText.NetworkWelcomeMessageHTML);
	}
	
	
	
	// Creates process, starts running it
	public static void main(String [ ] args) {
//		System.out.println("SimpleHttpServer Main Driver");
//		SimpleHttpServlet httpServer = new SimpleHttpServlet();
//		try {
//			httpServer.setupServer();
//			System.out.println("Server started");
//			// TODO: Determine how to keep the java server alive
//		} catch (Exception e) {
//			System.out.println("Failed to start Java HTTP server-- calling it quits");
//		}
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
				out = (new HtmlParser()).parse(jsonRequest);
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
