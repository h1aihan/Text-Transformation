package TextTransformation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

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
		server.createContext(Constants.Networking.infoAddress, new InfoHandler());
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
			boolean isGET = e.getRequestMethod().equals("GET");
			boolean isPOST = e.getRequestMethod().equals("POST");
			String stringQuery = e.getRequestURI().getQuery();
			
			/* ForwardHandler only handles GETs and POSTs; redirect to Info page*/
			if (!isPOST && !isGET) {
				writeBack(e, Constants.StaticText.NetworkWelcomeMessageHTML + "\nPlease use POST or GET.", HttpURLConnection.HTTP_BAD_REQUEST);
				e.close();
				return;
			} else if (stringQuery == null) {
				writeBack(e, Constants.StaticText.NetworkWelcomeMessageHTML + "\nPlease provide a string query.", HttpURLConnection.HTTP_BAD_REQUEST);
				e.close();
				return;
			}
			
			/* The POST request, as a JSON */
			JSONObject request = getQueryMap(stringQuery);
			JSONObject forward = new JSONObject();
			
			int status = HttpURLConnection.HTTP_BAD_REQUEST;
	
			boolean isForwardingToIndexing = request.has(Constants.JSON.indexingForwardAddressKey);
			boolean isForwardingLinks = request.has(Constants.JSON.linkForwardAddressKey);
			
			Output output;
			
//			if (!request.has(Constants.JSON.htmlInputKey) && !request.has("get")) {
//				writeBack(e, Constants.StaticText.NetworkDefaultError + "\nPlease provide HTML", sun.net.www.protocol.http.HttpURLConnection.HTTP_BAD_REQUEST);
//				return; 
//			}
			
			
			
			/* Determine if the request is valid, assemble request JSON */
			try {
				
				if (!request.has(Constants.JSON.metaDataKey))
					request.put(Constants.JSON.metaDataKey, new JSONObject());
				if (request.has("get_url")) {
					request.put(Constants.JSON.htmlInputKey, get(request.getString("get_url")).replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B"));
				}
				if (isPOST && !isForwardingToIndexing && !isForwardingLinks) 
					throw new Exception("POST request must include at least one forwarding address.");	
			} catch (Exception err) {
				/* Unrecoverable error-- invalid request! */
				status = HttpURLConnection.HTTP_BAD_REQUEST;
				writeBack(e, "{error : \"Cannot transform raw HTML into JSON: " + err.getMessage() + "\"}", status);
				e.close();
				return;
			} 

			/* Appears to be a good request, try to parse the included HTML */
			String getResponse = "None";
			try {

				output = Parser.HtmlParser.parse(request);

				forward.put(Constants.JSON.metaDataKey, output.getMetaDataJSON());

				getResponse = output.toString();
			} catch (Exception err) {
				/* Unrecoverable error-- unable to parse! */
				status = HttpURLConnection.HTTP_SEE_OTHER;
				writeBack(e, "{error : \"Cannot parse HTML: " + err.getMessage() + "\"\n, "
						+ "request: " + request + "}", status);
				e.close();
				return;
			}
			
			if (isGET) {
				writeBack(e, getResponse);
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
		
		public static JSONObject getQueryMap(String query)  
		{  
		    String[] params = query.split("&");  
		    JSONObject map = new JSONObject();  
		    for (String param : params)  
		    {  
		        String name = param.split("=")[0];  
		        String value = param.split("=")[1];  
		        try {
		        	if (name.length() > 2) 
		        		map.put(name, value);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		        
		    }  
		    return map;  
		}
		
		public static String get(String surl) {
			StringBuffer response = new StringBuffer();
			HttpURLConnection connection = null;
			
			try {
				
				CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
				connection = (HttpURLConnection)new URL(surl).openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36");
				System.out.println("\nSending 'GET' request to URL : " + surl);
			    System.out.println("Response Code : " + connection.getResponseCode());
			    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    String inputLine;
			    

			    while ((inputLine = in.readLine()) != null) {
			        response.append(inputLine);
			    }
			    in.close();
//				BufferedReader in = new BufferedReader(
//				        new InputStreamReader(connection.getInputStream()),
//			  			
//				        String inputLine;
//				        while ((inputLine = in.readLine()) != null)
//				            content += inputLine;
//				        in.close();
			}catch ( Exception ex ) {
			    ex.printStackTrace();
			}
			System.out.println(response.toString());
			return response.toString();
			
		}
		
		private static void writeBack(HttpExchange c, String out) {
			writeBack(c, out, HttpURLConnection.HTTP_OK);
		}
		
		
		private static void writeBack(HttpExchange c, String out, int HttpResponse) {
			try {
				c.sendResponseHeaders(HttpResponse, 0);
				OutputStream outStream = c.getResponseBody();
				outStream.write(out.getBytes());
				outStream.close();
			} catch (Exception err) {
				err.printStackTrace();
				return;
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



