package TestTextTransformation;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.Headers;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import TextTransformation.Constants;
import TextTransformation.SimpleHttpServer;

public class UnitTestNetworkHandler {
	SimpleHttpServer server = new SimpleHttpServer();
	
	@Before
	public void setUp() throws Exception {
		server.setupServer();
		
	}

	@After
	public void tearDown() throws Exception {
		server.stopServer();
	}
	
	private HttpURLConnection getResponse(String request) throws Exception {
		
		URL url;
		HttpURLConnection con;
		url = new URL("http://127.0.0.1:" + Constants.Networking.socketAddress.getPort() + request);
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		return con;
	}
	
	private String getStringResponse(HttpURLConnection con) {
		ArrayList<String> content = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content.add(inputLine);
			
			}
			in.close();
		} catch (Exception e) {
			//e.printStackTrace();
			return "{response: \"No string response\"}";
		}
		String response = StringUtils.join(content, "\n");
		return response;
		
	}
	
	@Test
	public void testInfo()  {
		HttpURLConnection con;
		String response;
		try {
			 con = getResponse("/info");
			 con.setRequestMethod("GET");
			 assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
			 response = getStringResponse(con);
			 assertEquals(response, Constants.StaticText.NetworkWelcomeMessage);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
			return;
		}		
		con = null;
		try {
			 con = getResponse("/info");
			 con.setRequestMethod("POST");
			 assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
			 assertEquals(Constants.StaticText.NetworkWelcomeMessage, getStringResponse(con));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
			return;
		}		
	}
	
	
	@Test
	public void testBadEmptyRequest() {
		HttpURLConnection con;
		int httpResponse = HttpURLConnection.HTTP_NOT_FOUND;
		try {
			con = getResponse(Constants.Networking.transformAndForward);
			con.setRequestMethod("POST");
			httpResponse = con.getResponseCode();
		} catch (Exception err) {
			fail("Bad BadEmpty response");
			return;
		}
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, httpResponse);
	}
	
	@Test
	public void testGoodRequest() {
		HttpURLConnection con;
		String response = "Not initialized";
		String request = "Not initialized";
		JSONObject jsonResponse;
		int httpResponse = -1;
		try {
			request = Constants.Networking.transformAndForward + "?" + Constants.JSON.htmlInputKey + "=" + URLEncoder.encode(UnitTestHtmlParser.simpleHtmlString, "UTF-8");
			con = getResponse(request);
			con.setRequestMethod("GET");
			
			response = getStringResponse(con);
			jsonResponse = new JSONObject(response);
			httpResponse = con.getResponseCode();
			JSONObject meta = new JSONObject(jsonResponse.get("meta").toString());
			JSONArray links = jsonResponse.getJSONArray("links");
			
			assertEquals(1, links.length());
			assertEquals(HttpURLConnection.HTTP_OK, httpResponse);
		
		} catch (JSONException err) {
			fail("Failed Good request because of non-JSON response:\n" + response 
					+ "\nRequest: " + request 
					+ "\nJSONException: " + err.getMessage());
			return;
		} catch (Exception err) {
			fail("Failed Good request");
			return;
		} 
		
	}
	@Test
	public void testGoodExternalRequest() {
		HttpURLConnection con;
		String response = "Not initialized";
		String request = "Not initialized";
		JSONObject jsonResponse;
		int httpResponse = -1;
		try {
			request = "/?html_url=https://science.rpi.edu/computer-science";
			con = getResponse(request);
			con.setRequestMethod("GET");
			
			response = getStringResponse(con);
			jsonResponse = new JSONObject(response);
			httpResponse = con.getResponseCode();
			JSONObject meta = new JSONObject(jsonResponse.get("meta").toString());
			JSONArray links = jsonResponse.getJSONArray("links");
			
			assertEquals(82, links.length());
			assertEquals(HttpURLConnection.HTTP_OK, httpResponse);
		
		} catch (JSONException err) {
			fail("Failed Good request because of non-JSON response:\n" + response 
					+ "\nRequest: " + request 
					+ "\nJSONException: " + err.getMessage());
			return;
		} catch (Exception err) {
			err.printStackTrace();

			fail("Failed Good request");
			return;
		} 
	}
	

}