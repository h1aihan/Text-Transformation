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
	public static String simpleHtmlString="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\r\n" + 
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
	
	@Test
	// test this output data structure 
	public void testOutputDataStructure() throws Exception{
		this.json.put("html",simpleHtmlString);
		HtmlParser.Parser.parse(json);
	}

	@Test
	public void testUrlParser() {
		//Test simple HtmlString
		HashSet<String> links=HtmlParser.Parser.parseUrl(simpleHtmlString);
		System.out.println(links); 
		return;
	}
	
	
	@Test
	public void testRemoveTagAndBody() {
		String text = "<script> hello </script>";
		String result = HtmlParser.Parser.removeTagAndBody(text, "script");
		assertEquals(result, "");
	}
	
	// Each irrelevant tag is replaced with a space!	
	@Test
	public void testCleanupTags() {
		String html = "<body><b>Hello<p>World!</p></body>";
		String result = HtmlParser.Parser.cleanupTags(html);
		assertEquals(result, "  Hello World!  ");
	}
	
	@Test
	public void testIsTag() {
		String tag = "<p>";
		assert(HtmlParser.Parser.isTag(tag));
	}
	
	@Test
	public void testNotTag() {
		String notTag = "not";
		assertFalse(HtmlParser.Parser.isTag(notTag));
	}
	
	@Test
	public void testIsOpeningTag() {
		String openingTag = "<body>";
		String closingTag = "</body>";
		assertTrue(HtmlParser.Parser.isOpeningTag(openingTag));
		assertFalse(HtmlParser.Parser.isOpeningTag(closingTag));
	}
	
	@Test
	public void testGetTagName() {
		String tag1 = "<p>";
		String tag2 = "</p>";
		String tag3 = "<img/>";
		String tag4 = "<a href=blah >";
		
		String result1 = HtmlParser.Parser.getTagName(tag1);
		String result2 = HtmlParser.Parser.getTagName(tag2);
		String result3 = HtmlParser.Parser.getTagName(tag3);
		String result4 = HtmlParser.Parser.getTagName(tag4);
		
		assertEquals(result1, "p");
		assertEquals(result2, "p");
		assertEquals(result3, "img");
		assertEquals(result4, "a");
	}
	
	@Test
	public void testParseSimpleText() {
		String text = "abc";
		ArrayList<String> result = HtmlParser.Parser.parse(text);
		assert(result.size() == 1);
		assert(result.contains("abc"));
	}
	
	@Test
	public void testParseSimpleHtml() {
		String html = "<body><p>hello world</p></body>";
		ArrayList<String> result = HtmlParser.Parser.parse(html);;
		assert(result.size() == 2);
		assert(result.contains("hello"));
		assert(result.contains("world"));
	}
	
	@Test
	public void testGetNgramsSimpleText() {
		String text = "one two three";
		ArrayList<String> parsed = HtmlParser.Parser.parse(text);
		
		HashMap<String, NgramMap> ngrams = HtmlParser.Parser.createNgrams(parsed);
		NgramMap allNgrams = ngrams.get("all");
		
		String[] expectedResults = {"one", "two", "three", "one two", "two three", "one two three"};
		for (int i=0; i<expectedResults.length; i++) {
			assert(allNgrams.contain(expectedResults[i]));
		}
	}
	
	@Test
	public void testGetNgramsWithTitleAndHeader() {
		String text = "one <title>two</title> three";
		ArrayList<String> parsed = HtmlParser.Parser.parse(text);
		
		HashMap<String, NgramMap> ngrams = HtmlParser.Parser.createNgrams(parsed);
		NgramMap titleNgrams = ngrams.get("title");
		assert(titleNgrams.contain("two"));
		assertFalse(titleNgrams.contain("one two"));
		assertFalse(titleNgrams.contain("two three"));
	}
	
	
}
