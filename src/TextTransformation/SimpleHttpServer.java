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
			e.close();
		}
	}
	
	/**
	 * ForwardHandler attempts to parse HTML included in a POST request,
	 * then forward the response to the URL(s) defined in the request.
	 */
	static class ForwardHandler implements HttpHandler {	
		
		/**
		 * Handle TextTransformation requests, including forwards
		 * @param e the HttpExchange object connecting to the requester
		 *   		if <b>e</b> is not a POST request, delegates request to InfoHandler<br>
		 *       If <b>e</b> does not provide any forwarding addresses, responds with <b>HTTP_BAD_REQUEST</b><br>
		 *       If <b>e</b> fails to forward to all forwarding addresses, responds with <b>HTTP_BAD_REQUEST</b><br>
		 *       If <b>e</b> fails to forward to one address, but succeeds in forwarding to another, responds with <b>HTTP_PARTIAL</b><br>
		 *       If the server cannot parse the HTML in the request, responds with <b>HTTP_INTERNAL_ERROR</b><br>
		 */
		public void handle(HttpExchange e) throws IOException {
			String requestMethod = e.getRequestMethod();
			/* ForwardHandler only handles POSTs; redirect to Info page*/
			if (!requestMethod.equals("POST") && requestMethod.equals("GET")) {
				new InfoHandler().handle(e);
				return;
			}
			/* The POST request, as a JSON */
			JSONObject request;
			JSONObject forward = new JSONObject();
			int status = HttpURLConnection.HTTP_BAD_REQUEST;
			boolean isForwardingToIndexing;
			boolean isForwardingLinks;
			Output output;
			
			/* Determine if the request is valid */
			try {
				request = new JSONObject(e.getRequestHeaders());
				
				isForwardingToIndexing = request.has(Constants.JSON.indexingForwardAddressKey);
				isForwardingLinks = request.has(Constants.JSON.linkForwardAddressKey);
				if (!isForwardingToIndexing && !isForwardingLinks && !requestMethod.equals("GET")) 
					throw new Exception("ERROR: POST request must include at least one forwarding address.");
			} catch (Exception err) {
				/* Unrecoverable error-- invalid request! */
				status = HttpURLConnection.HTTP_BAD_REQUEST;
				e.sendResponseHeaders(status, 0);
				e.close();
				return;
			} 
			
			/* Appears to be a good request, try to parse the included HTML */
			try {
				output = Parser.HtmlParser.parse(request);
				if (requestMethod.equals("GET")) {
					writeBack(e, output.toString());
				}
				forward.put(Constants.JSON.metaDataKey, output.getMetaDataJSON());
			} catch (Exception err) {
				/* Unrecoverable error-- unable to parse! */
				status = HttpURLConnection.HTTP_INTERNAL_ERROR;
				e.sendResponseHeaders(status, 0);
				e.close();
				return;
			}
			
			/* If forward URL provided, attempt to forward links found in parsed HTML */
			if (isForwardingLinks) {
				try {					
					URL linkURL = new URL(request.getString(Constants.JSON.linkForwardAddressKey));
					forward.put(Constants.JSON.linksKey, output.getLinksJSON());
					send(forward.toString(), linkURL);
					status = HttpURLConnection.HTTP_OK;
				} catch (Exception err) { 
					/* Recoverable error-- cannot send links */
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
					status = HttpURLConnection.HTTP_OK;
				} catch (Exception err) { 
					/* If two recoverable errors--> bad request! */
					status = status == HttpURLConnection.HTTP_PARTIAL ? 
										HttpURLConnection.HTTP_BAD_REQUEST : 
										HttpURLConnection.HTTP_PARTIAL;
				}
			}

			e.sendResponseHeaders(status, 0);
			e.close();
		}
		
		
		private static void writeBack(HttpExchange c, String out) {
			try {
				OutputStream outStream = c.getResponseBody();
	            outStream.write(out.getBytes());
	            outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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
