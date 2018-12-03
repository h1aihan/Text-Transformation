package TextTransformation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//import javax.servlet.*;
//import javax.servlet.http.*;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// SimpleHttpServer creates a process to handle all requests
//		It responds to requests in InfoHandler, NetworkHandler, & ForwardHandler
// See https://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html 
public class SimpleHttpServer {
	/* */
//	private static final long serialVersionUID = 8021309171908246830L; /* */
	private HttpServer server;
	
	/**
	 * Creates and starts the HttpServer
	 * 
	 * @modifies server initializes new server sockets, addresses listed in Constants
	 * @effects a HttpServer is started on LocalHost 
	 * @throws Exception if server cannot be created or started
	 */
	public void setupServer() throws Exception {
		/* Avoids multiple processes attempting to listen on the same port */
		stopServer();
		/* Creates Server listening on port: Constant socketAddress */
		server = HttpServer.create(Constants.Networking.socketAddress, 0);
		/* Sets root address handler, i.e. the object responsible for
		 * connections made to the server 								
		 * Currently, rootAddress -> InfoHandler */
		server.createContext(Constants.Networking.rootAddress, new InfoHandler());
		/* 			  transformAndForward -> ForwardHandler*/
		server.createContext(Constants.Networking.transformAndForward, new ForwardHandler());
		/* Default executor (requests handled sequentially, until further development) */
		server.setExecutor(null); 
		server.start();
	}
	/**
	 * Stops the currently running server, if it exists
	 * 
	 * @modifies server if not null, stops this server immediately
	 * @effects Frees port server is currently listening on
	 */
	public void stopServer() {
		if (server == null)
			return;
		else 
			server.stop(0);
	}
	
	/**
	 * HttpServer driver main, runs server as a process
	 * 
	 * @effects starts a SimpleHttpServer on localhost, prints status
	 */
	public static void main(String [ ] args) {
		System.out.println("SimpleHttpServer Main Driver");
		SimpleHttpServer httpServer = new SimpleHttpServer();
		try {
			httpServer.setupServer();
			System.out.println("HttpServer successfully started on " + httpServer.server.getAddress());
		} catch (Exception e) {
			System.out.println("Failed to start Java HTTP server:");
			e.printStackTrace();
		}
	}
	
	/**
	 * InfoHandler responds to all requests with a default page
	 * 
	 * @effects upon receiving an HttpExchange, responds with welcome page (NetworkWelcomeMessage) 
	 * @throws IOException if handler cannot respond
	 */	
	static class InfoHandler implements HttpHandler {
		public void handle(HttpExchange e) throws IOException {
			String response = Constants.StaticText.NetworkWelcomeMessage;
			e.sendResponseHeaders(200, response.length());
			OutputStream os = e.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	/**
	 * ForwardHandler attempts to parse HTML included in a POST request,
	 * then forward the response to the URL(s) defined in the request 
	 *
	 */
	static class ForwardHandler implements HttpHandler {	
		
		/**
		 * Handle TextTransformation requests, including forwards
		 * 
		 * @param e the HttpExchange object connecting to the requester
		 * 
		 * @effects if e is not a POST request, delegates request to InfoHandler
		 *          if e does not provide any forwarding addresses, responds with HTTP_BAD_REQUEST
		 *          if e fails to forward to all forwarding addresses, responds with HTTP_BAD_REQUEST
		 *          if e fails to forward to one address, but succeeds in forwarding to another, responds with HTTP_PARTIAL
		 *          if the server cannot parse the HTML in the request, responds with HTTP_INTERNAL_ERROR
		 */
		public void handle(HttpExchange e) throws IOException {
			/* ForwardHandler only handles POSTs; redirect to Info page*/
			if (e.getRequestMethod() != "POST") {
				new InfoHandler().handle(e);
				return;
			}
			/* The POST request, as a JSON */
			JSONObject request;
			JSONObject forward = new JSONObject();
			int httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
			boolean isForwardingToIndexing;
			boolean isForwardingLinks;
			Output output;
			
			/* Determine if the request is valid */
			try {
				request = new JSONObject(e.getRequestBody());
				isForwardingToIndexing = request.has(Constants.JSON.indexingForwardAddressKey);
				isForwardingLinks = request.has(Constants.JSON.linkForwardAddressKey);
				if (!isForwardingToIndexing && !isForwardingLinks) 
					throw new Exception("ERROR: Request must include at least one forwarding address.");
			} catch (Exception err) {
				// Unrecoverable error-- invalid request!
				httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
				e.sendResponseHeaders(httpStatus, 0);
				return;
			} 
			
			
			/* Appears to be a good request, try to parse the included HTML */
			try {
				output = HtmlParser.Parser.parse(request);
				forward.put(Constants.JSON.metaDataKey, output.getMetaDataJSON());
			} catch (Exception err) {
				// Unrecoverable error-- unable to parse!
				httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
				e.sendResponseHeaders(httpStatus, 0);
				return;
			}
			
			/* If forward URL provided, attempt to forward links found in parsed HTML */
			if (isForwardingLinks) {
				try {					
					URL linkURL = new URL(request.getString(Constants.JSON.linkForwardAddressKey));
					forward.put(Constants.JSON.linksKey, output.getLinksJSON());
					send(forward.toString(), linkURL);
					httpStatus = HttpURLConnection.HTTP_OK;
				} catch (Exception err) { 
					// Recoverable error-- cannot send links
				}
			}
			/* If forward URL provided, attempt to forward parsed HTML */
			if (isForwardingToIndexing) {
				try {
					URL indexURL = new URL(request.getString(Constants.JSON.indexingForwardAddressKey));
					if (forward.has(Constants.JSON.linksKey)) 
						forward.remove(Constants.JSON.linksKey);

					forward.put(Constants.JSON.indexingForwardAddressKey, output.getNGramJSON());
					send(forward.toString(), indexURL);
					httpStatus = HttpURLConnection.HTTP_OK;
				} catch (Exception err) { 
					// If two recoverable errors--> bad request!
					httpStatus = httpStatus == HttpURLConnection.HTTP_PARTIAL ? 
							HttpURLConnection.HTTP_BAD_REQUEST : 
								HttpURLConnection.HTTP_PARTIAL;
				}
			}
			e.sendResponseHeaders(httpStatus, 0);
		}
		
		
		private static HttpURLConnection makePostConnection(URL url) throws Exception {
			HttpURLConnection post = (HttpURLConnection) url.openConnection();
			post.setRequestMethod("POST");
			post.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			post.setRequestProperty("Content-Language", "en-US");
			post.setDoInput(false);
			post.setDoOutput(true);
			return post;
		}
		
		// Send string parameters "parameters" to the URL "to"
		private void send(String parameters, URL destination) throws Exception {
			HttpURLConnection post = ForwardHandler.makePostConnection(destination);

			try {
				post.setRequestProperty("Content-Length", "" + 
						Integer.toString(parameters.getBytes().length));
				
				DataOutputStream wr = new DataOutputStream (
						post.getOutputStream ());
				wr.writeBytes(parameters);
				wr.flush ();
				wr.close ();
				InputStream is = post.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuffer response = new StringBuffer(); 
				while((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				rd.close();
			} catch (Exception e) {

			} finally {
				post.disconnect();
			}
		}
		
	}
	
//	/* DEPRECATED: Servlet Functions
//	 * 
//	 * 
//	 */
//	public void doGet(HttpServletRequest request, 
//			  HttpServletResponse response) 
//			  throws ServletException, IOException {
//			  PrintWriter out = response.getWriter();
//			  out.println(Constants.StaticText.NetworkWelcomeMessageHTML);
//	}
	
}
