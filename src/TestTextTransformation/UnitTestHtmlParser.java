package TestTextTransformation;
import TextTransformation.*;

import org.apache.commons.lang3.StringUtils;
import org.json.*;
import org.junit.*;

import static org.junit.Assert.*;
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
	
	@Before
	public void setUp() {
		parser1 = new HtmlParser();
	}
	
	public void testConstructor() {
		//Implement
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
	
	
	@Test
	public void testRemoveTagAndBody() {
		String text = "<script> hello </script>";
		String result = parser1.removeTagAndBody(text, "script");
		assertEquals(result, "");
	}
	
	// Each irrelevant tag is replaced with a space!	
	@Test
	public void testCleanupTags() {
		String html = "<body><b>Hello<p>World!</p></body>";
		String result = parser1.cleanupTags(html);
		assertEquals(result, "  Hello World!  ");
	}
	
	@Test
	public void testIsTag() {
		String tag = "<p>";
		assert(parser1.isTag(tag));
	}
	
	@Test
	public void testNotTag() {
		String notTag = "not";
		assertFalse(parser1.isTag(notTag));
	}
	
	@Test
	public void testIsOpeningTag() {
		String openingTag = "<body>";
		String closingTag = "</body>";
		assertTrue(parser1.isOpeningTag(openingTag));
		assertFalse(parser1.isOpeningTag(closingTag));
	}
	
	@Test
	public void testGetTagName() {
		String tag1 = "<p>";
		String tag2 = "</p>";
		String tag3 = "<img/>";
		String tag4 = "<a href=blah >";
		
		String result1 = parser1.getTagName(tag1);
		String result2 = parser1.getTagName(tag2);
		String result3 = parser1.getTagName(tag3);
		String result4 = parser1.getTagName(tag4);
		
		assertEquals(result1, "p");
		assertEquals(result2, "p");
		assertEquals(result3, "img");
		assertEquals(result4, "a");
	}
	
	@Test
	public void testParseSimpleText() {
		String text = "abc";
		parser1.parse(text);
		ArrayList<String> result = parser1.getWords();
		assert(result.size() == 1);
		assert(result.contains("abc"));
	}
	
	@Test
	public void testParseSimpleHtml() {
		String html = "<body><p>hello world</p></body>";
		parser1.parse(html);
		ArrayList<String> result = parser1.getWords();
		assert(result.size() == 2);
		assert(result.contains("hello"));
		assert(result.contains("world"));
	}
	
	@Test
	public void testGetNgramsSimpleText() {
		String text = "one two three";
		parser1.parse(text);
		parser1.createNgrams();
		
		HashMap<String, NgramMap> ngrams = parser1.getNgrams();
		NgramMap allNgrams = ngrams.get("all");
		
		String[] expectedResults = {"one", "two", "three", "one two", "two three", "one two three"};
		for (int i=0; i<expectedResults.length; i++) {
			assert(allNgrams.contain(expectedResults[i]));
		}
	}
	
	@Test
	public void testGetNgramsWithTitleAndHeader() {
		String text = "one <title>two</title> three";
		parser1.parse(text);
		parser1.createNgrams();
		
		HashMap<String, NgramMap> ngrams = parser1.getNgrams();
		NgramMap titleNgrams = ngrams.get("title");
		assert(titleNgrams.contain("two"));
		assertFalse(titleNgrams.contain("one two"));
		assertFalse(titleNgrams.contain("two three"));
	}
	
	
}
