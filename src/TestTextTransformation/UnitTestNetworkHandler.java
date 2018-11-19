package TestTextTransformation;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.lang3.StringUtils;
import TextTransformation.Constants;
import TextTransformation.SimpleHttpServlet;

public class UnitTestNetworkHandler {
	SimpleHttpServlet server = new SimpleHttpServlet();
	
	@Before
	public void setUp() throws Exception {
		//server.setupServer();
		fail("Need to refactor tests");
	}

	@After
	public void tearDown() throws Exception {
		//server.stopServer();
	}

	
	@Test
	public void testInfo()  {
		URL url;
		HttpURLConnection con;
		try {
			url = new URL("http://127.0.0.1:" + Constants.Networking.socketAddress.getPort());
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
		} catch (Exception e) {
			fail("Could not create connection with self");
			return;
		}
		try {
			assert(con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (IOException e) {
			fail("Did not receive reasonable response");
		}
//		StringBuffer content = new StringBuffer();
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
			fail("Problems reading response");
		}
		String response = StringUtils.join(content, "\n");

		assert(response.equals(Constants.StaticText.NetworkWelcomeMessage));
	}
	
	@Test
	public void testRequest() {
		
		URL url;
		URLConnection c;
		HttpURLConnection httpConnection;
		try {
			url = new URL("http://127.0.0.1:" + Constants.Networking.socketAddress.getPort());
			c = url.openConnection();
			httpConnection = (HttpURLConnection)c;
			httpConnection.setRequestMethod("POST"); // PUT is another valid option
			httpConnection.setDoOutput(true);
		} catch (Exception e) {
			fail("Failed to create request");
			return;
		}
		HashMap<String,String> arguments = new HashMap<String, String>();
		// TODO: Set arguments
		
		/*
		 * 
		 * { 
			 metadata: {
			      url: "www.rpi.edu"
			      access_time: "2018-10-9T10:00:00Z" (ISO 8061)
			      forward_address: "some.aws.address.com/...." or NULL
			            }
			  content: { "<doctype HTML!>....</HTML>" }
			}
		 * 
		 */
		
		StringJoiner joinedParams = new StringJoiner("&");
		for(HashMap.Entry<String,String> entry : arguments.entrySet()) {
			try {
				joinedParams.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						+ URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {}
		}
		byte[] out = joinedParams.toString().getBytes(StandardCharsets.UTF_8);
		int length = out.length;

		httpConnection.setFixedLengthStreamingMode(length);
		httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		try {
			httpConnection.connect();
			try(OutputStream os = httpConnection.getOutputStream()) {
				os.write(out);
			}

		} catch (Exception e){
			fail("Could not send request");
		}

	}

}
