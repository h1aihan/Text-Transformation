package TextTransformation;
import java.lang.Math;
import java.lang.String;
import java.util.*;
import org.json.*;


// TODO: Consider making static
//TODO: Document class definition
public class HtmlParser {
	// Comment.
	private static final String delimiters = "[ .,-\";?!/\\]+";
	private static final String[] prioritizedTags = {"title", "p", "h1", "h2", "h3", "h4", "h5", "h6", ""};
	private ArrayList<String> words;
	
	// TODO: Implement
	public void parse(String text) {
		// Convert to lower-case
		text = text.toLowerCase();

		// Removal of unwanted tags
		text = removeTagAndBody(text, "style");
		text = removeTagAndBody(text, "script");
		text = cleanupTags(text);
		
		// Split string into words
		this.words = new ArrayList<String>(Arrays.asList(text.split(delimiters)));
		
		// Remove if length < 2
		words.removeIf(word -> word.length() < 2);
		
		// Check for certain allowed punctuation
		//		-> only single quotation mark is allowed
	}
	
	// Edge cases - tags where we don't want the in between stuff.
	public String removeTagAndBody(String text, String tagName) {
		StringBuffer buffer = new StringBuffer(text);
		int open = buffer.indexOf("<"+tagName, 0);
		int close = buffer.indexOf("</" + tagName + ">", 0);
		while (open != -1) {
			buffer.replace(open, close, "");
			open = buffer.indexOf("<" + tagName, close);
			close = buffer.indexOf("</" + tagName + ">", close);
		}
		return buffer.toString();
	}
	
	public String cleanupTags(String text) {
		StringBuffer buffer = new StringBuffer(text);
		int open = buffer.indexOf("<", 0);
		int close = buffer.indexOf(">", 0);
		String tag;
		while (open != -1) {
			tag = buffer.substring(open, close+1);
			if (!Arrays.asList(prioritizedTags).contains(getTagName(tag))) {
				// Remove tag
				buffer.replace(open, close, "");
			} else {
				// Remove any modifiers/extraneous spacing on tag
				// <tag mod=""> --> <tag>
				int mod = tag.indexOf(" ", open);
				if (mod != -1) {
					if (tag.charAt(tag.length() - 2) == '/') {
						buffer.replace(open + mod, close - 1, "");
					}
					else {
						buffer.replace(open + mod, close, "");
					}
				}
			}
	
			open = buffer.indexOf("<", open);
			close = buffer.indexOf(">", open);
		}
		return buffer.toString();
	}
	
	// Expected input --> <tag> or <tag modifier="m"> or </tag> or <tag/>
	public String getTagName(String tag) {
		int start, end;
		if (tag.startsWith("</")) {
			start = 2;
		} else if (tag.startsWith("<")) {
			start = 1;
		} else {
			// Not the correct input!
			return "";
		}
		end = tag.indexOf(">");
		if (tag.indexOf(" ") != -1) {
			end = Math.min(end, tag.indexOf(" "));
		}
		if (tag.indexOf("/") != -1) {
			end = Math.min(end, tag.indexOf("/"));
		}
		return tag.substring(start, end);
	}
	
	public OutputDataStructure parse(JSONObject json) throws Exception {
		// TODO: Implement
		throw new Exception();
	}
	
}
