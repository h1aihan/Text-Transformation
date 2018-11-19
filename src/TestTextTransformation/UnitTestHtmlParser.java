package TestTextTransformation;
import TextTransformation.*;

import org.apache.commons.lang3.StringUtils;
import org.json.*;
import org.junit.*;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
public class UnitTestHtmlParser {
	private HtmlParser parser1;
	private JSONObject json= new JSONObject(); 
	private String simpleUrlSegment="hello this is a url Linkedin.com";
	private String simpleHtmlString="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\r\n" + 
			" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n" + 
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\r\n" + 
			"  <head>\r\n" + 
			"    <title>Your page title here</title>\r\n" + 
			"  </head>\r\n" + 
			"  <body>\r\n" + 
			"    <h1>Your major heading here</h1>\r\n" + 
			"    <p>\r\n" + 
			"      This is a regular text paragraph.\r\n" + 
			"    </p>\r\n" + 
			"    <ul>\r\n" + 
			"      <li>\r\n" + 
			"        First bullet of a bullet list.\r\n" + 
			"      </li>\r\n" + 
			"      <li>\r\n" + 
			"        This is the <em>second</em> bullet. \r\n" + 
			"      </li>\r\n" + 
			"    </ul>\r\n" + 
			"  </body>\r\n" + 
			"</html>";
	public void testConstructor() {
		//Implement
		fail("No implementation");
		return;
	}
	@Test
	// TODO: Implement
	public void testParse() {
		fail("No implementation");
		return;
	}
	@Test
	// TODO: Implement
	public void testCreateNgrams() {
		fail("No implementation");
		return;
	}
	@Test
	// TODO:Implement 
	public void testRemoveTagAndBody() {
		fail("No implementation");
		return;
	}
	@Test
	// TODO:Implement
	public void testCleanupTags() {
		fail("No implementation");
		return;
	}
	@Test
	//TODO: Implement
	public void testGetTagName() {
		fail("No implementation");
		return;
	}
	
	@Test
	// TODO:Implement
	// fix: null pointer exception
	public void testOutputDataStructure() throws Exception{
		this.json.put("content",simpleHtmlString);
		parser1.parse(json);
	}
	@Test
	public void testUrlParser() {
		//Implement
		fail("No implementation");
		return;
	}
}
