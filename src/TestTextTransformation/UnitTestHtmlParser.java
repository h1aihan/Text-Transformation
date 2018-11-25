package TestTextTransformation;
import TextTransformation.*;
import org.junit.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.linkedin.urls.*;
public class UnitTestHtmlParser {
	private HtmlParser hp1;
	private String htmlsegment1="hello this is a url Linkedin.com";
	
	@Before
	public void setUp() {
		hp1 = new HtmlParser();
	}
	
	@Test
	public void testUrlParser() {
	}
	
	
	@Test
	public void testRemoveTagAndBody() {
		String text = "<script> hello </script>";
		String result = hp1.removeTagAndBody(text, "script");
		assertEquals(result, "");
	}
	
	// Each irrelevant tag is replaced with a space!	
	@Test
	public void testCleanupTags() {
		String html = "<body><b>Hello<p>World!</p></body>";
		String result = hp1.cleanupTags(html);
		assertEquals(result, "  Hello World!  ");
	}
	
	@Test
	public void testIsTag() {
		String tag = "<p>";
		assert(hp1.isTag(tag));
	}
	
	@Test
	public void testNotTag() {
		String notTag = "not";
		assertFalse(hp1.isTag(notTag));
	}
	
	@Test
	public void testIsOpeningTag() {
		String openingTag = "<body>";
		String closingTag = "</body>";
		assertTrue(hp1.isOpeningTag(openingTag));
		assertFalse(hp1.isOpeningTag(closingTag));
	}
	
	@Test
	public void testGetTagName() {
		String tag1 = "<p>";
		String tag2 = "</p>";
		String tag3 = "<img/>";
		String tag4 = "<a href=blah >";
		
		String result1 = hp1.getTagName(tag1);
		String result2 = hp1.getTagName(tag2);
		String result3 = hp1.getTagName(tag3);
		String result4 = hp1.getTagName(tag4);
		
		assertEquals(result1, "p");
		assertEquals(result2, "p");
		assertEquals(result3, "img");
		assertEquals(result4, "a");
	}
	
	@Test
	public void testParseSimpleText() {
		String text = "abc";
		hp1.parse(text);
		ArrayList<String> result = hp1.getWords();
		assert(result.size() == 1);
		assert(result.contains("abc"));
	}
	
	@Test
	public void testParseSimpleHtml() {
		String html = "<body><p>hello world</p></body>";
		hp1.parse(html);
		ArrayList<String> result = hp1.getWords();
		assert(result.size() == 2);
		assert(result.contains("hello"));
		assert(result.contains("world"));
	}
	
	@Test
	public void testGetNgramsSimpleText() {
		String text = "one two three";
		hp1.parse(text);
		hp1.createNgrams();
		
		HashMap<String, NgramMap> ngrams = hp1.getNgrams();
		NgramMap allNgrams = ngrams.get("all");
		
		String[] expectedResults = {"one", "two", "three", "one two", "two three", "one two three"};
		for (int i=0; i<expectedResults.length; i++) {
			assert(allNgrams.contain(expectedResults[i]));
		}
	}
	
	@Test
	public void testGetNgramsWithTitleAndHeader() {
		String text = "one <title>two</title> three";
		hp1.parse(text);
		hp1.createNgrams();
		
		HashMap<String, NgramMap> ngrams = hp1.getNgrams();
		NgramMap titleNgrams = ngrams.get("title");
		assert(titleNgrams.contain("two"));
		assertFalse(titleNgrams.contain("one two"));
		assertFalse(titleNgrams.contain("two three"));
	}
	
	
}
