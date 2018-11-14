package TextTransformation;
import java.lang.Math;
import java.lang.String;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

// TODO: Consider making static
//TODO: Document class definition
public class HtmlParser {
	// Comment.
	private static final String delimiters = "[ .!?,;-]+";
	private static final String[] prioritizedTags = {"title", "p", "h1", "h2", "h3", "h4", "h5", "h6"};
	private ArrayList<String> words;
	private HashMap<String, NgramMap> ngrams;
	
	public HtmlParser() {
		this.words = new ArrayList<String>();
		this.ngrams = new HashMap<String, NgramMap>();
		ngrams.put("body", new NgramMap());
		for (String t: prioritizedTags) {
			ngrams.put(t, new NgramMap());
		}
	}
	
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
		int occurrences = 0;
		String tmp[];
		for (int i = 0; i < words.size(); i++) {
			occurrences = StringUtils.countMatches(words.get(i), '\'');
			if (occurrences > 1) {
				// Split the word
				tmp = words.get(i).split("\'");
				words.set(i, tmp[0] + "'" + tmp[1]);
				for (int j = 2; j < tmp.length; j++) {
					words.add(i+j-1, tmp[j]);
				}
			}
		}
	}
	
	// Comment.
	public void createNgrams() {
		NgramMap m;
		ArrayList<String> tmp_words = new ArrayList<String>();
		Stack<String> tags = new Stack<String>();
		tags.add("body");
		for (int i=0; i < words.size(); i++) {
			// Found a new tag
			if (isTag(words.get(i))) {
				// Add to stack
				if (isOpeningTag(words.get(i))) {
					tags.push(getTagName(words.get(i)));
				} else {
					// Pop from stack
					tags.pop();
				}
			} else {
				// Add word to the corresponding tag's ngram mapping 
				m = ngrams.get(tags.peek());
				m.insert(words.get(i));

				// 2grams -> 5grams
				tmp_words.add(words.get(i));
				for (int j = 2; j < 6; j++) {
					if (tmp_words.size() < j) {
						break;
					}
					m.insert(new ArrayList<String>(tmp_words.subList(tmp_words.size()-j, tmp_words.size())));
				}
				if (tmp_words.size() == 5) {
					tmp_words.remove(0);
				}
			}
		}
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
				buffer.replace(open, close+1, " ");
				open = buffer.indexOf("<", open);
			} else {
				// Remove any modifiers/extraneous spacing inside tag
				// 		<tag mod=""> --> <tag>
				// Also pad the tags with spaces to ensure that tags get separated from words
				// 		"word<tag>word" -> "word <tag> word"
				int mod = tag.indexOf(" ", open);
				if (mod != -1) {
					buffer.insert(close+1, " ");
					if (tag.charAt(tag.length() - 2) == '/') {
						buffer.replace(open + mod, close - 1, "");
					}
					else {
						buffer.replace(open + mod, close, "");
					}
					buffer.insert(open, " ");
					open = buffer.indexOf("<", open+mod);
				} else {
					buffer.insert(close+1, " ");
					buffer.insert(open, " ");
					open = buffer.indexOf("<", close);
				}
			}
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
		if (tag.indexOf("/") != -1 && start != 2) {
			end = Math.min(end, tag.indexOf("/"));
		}
		return tag.substring(start, end);
	}
	
	public boolean isTag(String word) {
		return word.startsWith("<") && word.endsWith(">");
	}
	
	public boolean isOpeningTag(String word) {
		return !word.startsWith("</");
	}

	public List<Url> parserUrl(String html){
		 UrlDetector parser = new UrlDetector(html, UrlDetectorOptions.Default);
		    List<Url> found = parser.detect();
		    return found;
	}
	
	public OutputDataStructure parse(JSONObject json) throws Exception {
		// TODO: Implement
		
		// Parse html
		String html = json.getString("content");
		parse(html);
		
		// Create the ngrams
		createNgrams();
		
		return new OutputDataStructure(this.ngrams);
	}
	
}
