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
	private static final String delimiters = "[ \".!?,;-]+";
	private static final String[] prioritizedTags = {"title", "h1", "h2", "h3", "h4", "h5", "h6"};
	private ArrayList<String> words;
	private HashMap<String, NgramMap> ngrams;
	
	public HtmlParser() {
		this.words = new ArrayList<String>();
		this.ngrams = new HashMap<String, NgramMap>();
		ngrams.put("all", new NgramMap());
		ngrams.put("headers", new NgramMap());
		ngrams.put("title", new NgramMap());
	}
	
	public ArrayList<String> getWords() {
		return new ArrayList<String>(this.words);
	}
	
	public HashMap<String, NgramMap> getNgrams() {
		return new HashMap<String, NgramMap>(this.ngrams);
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
		ArrayList<Integer> lengths = new ArrayList<Integer>();
		lengths.add(0);
		Stack<String> tags = new Stack<String>();
		tags.add("all");
		NgramMap all = ngrams.get("all");
		boolean special = false;	// Special case -> ngram is title or header

		for (int i=0; i < words.size(); i++) {
			// Found a new tag
			if (isTag(words.get(i))) {
				// Add to stack
				if (isOpeningTag(words.get(i))) {
					tags.push(getTagName(words.get(i)));
					lengths.add(0);
				} else {
					// Pop from stack
					tags.pop();
					lengths.remove(lengths.size() - 1);
				}
			} else {
				increment(lengths);
				// Add word to the corresponding tag's ngram mapping
				if (tags.peek().equals("title")) {
					m = ngrams.get("title");
					special = true;
				} else if (Arrays.asList(prioritizedTags).contains(tags.peek())){
					m = ngrams.get("headers");
					special = true;
				} else {
					m = null;
					special = false;
				}
				
				Set<String> stopWords=Constants.StaticCollections.StopWords;
				if (!stopWords.contains(words.get(i))) {
					if (special) {
						m.insert(words.get(i));
					}
					all.insert(words.get(i));
				}

				// 2grams -> 5grams
				tmp_words.add(words.get(i));
				for (int j = 2; j < 6; j++) {
					if (tmp_words.size() < j) {
						break;
					}
					if (special && lengths.get(lengths.size() - 1) >= j) {
						m.insert(new ArrayList<String>(tmp_words.subList(tmp_words.size()-j, tmp_words.size())));
					}
					all.insert(new ArrayList<String>(tmp_words.subList(tmp_words.size()-j, tmp_words.size())));
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
			buffer.replace(open, close + 3 + tagName.length(), "");
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
	
	public void increment(ArrayList<Integer> nums) {
		for (int i=0; i<nums.size(); i++) {
			nums.set(i, nums.get(i) + 1);
		}
	}

	public HashSet<String> parseUrl(String html){
		 UrlDetector parser = new UrlDetector(html, UrlDetectorOptions.Default);
		 HashSet<String> links=new HashSet<String>();
		    List<Url> found = parser.detect();
		    for(Url url : found) {
				links.add(url.getFullUrl());
			}
		    return links;
	}

	public OutputDataStructure parse(JSONObject json) throws Exception {
		// Parse html
		String html = json.getString("html");
		parse(html);
		HashSet<String> links=this.parseUrl(html);
		// Create the ngrams
		createNgrams();
		return new OutputDataStructure(this.ngrams,links);
	}
	
}
