package TextTransformation;
import java.lang.Math;
import java.lang.String;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

/**
 * Text Transformation HtmlParser class implements methods to parse raw html into ngrams.
 */

public final class Parser {
	
	public static class HtmlParser {
		/**
		 * Returns an OutputDataStructure with the parsed ngrams and links.
		 * 
		 * @param JSONObject json
		 * @return OutputDataStructure
		 */
		public static Output parse(JSONObject json) throws Exception {
			String html = json.getString(Constants.JSON.htmlInputKey);
			ArrayList<String> parsedWords = parse(html);
			HashSet<String> links= parseUrl(html);
			HashMap<String, NgramMap> ngrams = createNgrams(parsedWords);
			return new Output(ngrams,links);
		}
		
		/**
		 * Parses the given string:
		 * 		changes all characters to lowercase,
		 * 		removes unwanted tags,
		 * 		and puts all valid words into a list.
		 * 
		 * @param String text : The text to parse
		 */
		public static ArrayList<String> parse(String text) {
			// Convert to lower-case
			text = text.toLowerCase();

			// Removal of unwanted tags
			text = removeTagAndBody(text, "style");
			text = removeTagAndBody(text, "script");
			text = cleanupTags(text);
			
			// Split string into words
			ArrayList<String> words = new ArrayList<String>(Arrays.asList(text.split(Constants.Parsing.delimiters)));
			
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
			
			// Remove if length < 2
			words.removeIf(word -> word.length() < 2);
			
			return words;
		}
		
		/**
		 * Populates the ngram hashmap with 1-5grams using the parsed list of words.
		 */
		public static HashMap<String, NgramMap> createNgrams(ArrayList<String> words) {
			// Create and initialize new mapping from tags -> ngramMap
			HashMap<String, NgramMap>ngrams = new HashMap<String, NgramMap>();
			ngrams.put("all", new NgramMap());
			ngrams.put("headers", new NgramMap());
			ngrams.put("title", new NgramMap());
			
			NgramMap m;
			boolean isSpecial = false;	// Special case -> ngram is title or header
			ArrayList<String> wordsQueue = new ArrayList<String>();
			Stack<Integer> lengths = new Stack<Integer>();
			lengths.push(0);
			Stack<String> tags = new Stack<String>();
			tags.add("all");
			NgramMap all = ngrams.get("all");

			for (String word : words) {
				// Found a new tag
				if (isTag(word)) {
					// Add to stack
					if (isOpeningTag(word)) {
						tags.push(getTagName(word));
						lengths.push(0);
					} else {
						// Pop from stacks
						tags.pop();
						int last = lengths.pop();
						lengths.push(last + lengths.pop());
					}
				} else {
					lengths.push(lengths.pop() + 1);
					// Add word to the corresponding tag's ngram mapping
					if (tags.peek().equals("title")) {
						m = ngrams.get("title");
						isSpecial = true;
					} else if (Constants.Parsing.prioritizedTags.contains(tags.peek())){
						m = ngrams.get("headers");
						isSpecial = true;
					} else {
						m = null;
						isSpecial = false;
					}
					
					// Ignore if stop word.
					Set<String> stopWords=Constants.StaticCollections.StopWords;
					if (!stopWords.contains(word)) {
						if (isSpecial) {
							m.insert(word);
						}
						all.insert(word);
					}

					// 2grams -> 5grams
					wordsQueue.add(word);
					int queueSize = wordsQueue.size();
					for (int j = 2; j < 6; j++) {
						if (queueSize < j) {
							break;
						}
						if (isSpecial && lengths.peek() >= j) {
							m.insert(new ArrayList<String>(wordsQueue.subList(queueSize-j, queueSize)));
						}
						all.insert(new ArrayList<String>(wordsQueue.subList(queueSize-j, queueSize)));
					}
					if (queueSize == 5) {
						wordsQueue.remove(0);
					}
				}
			}
			return ngrams;
		}
		
		/**
		 * Finds and removes the given tag and body from String 'text'.
		 * Example: If tagName = "script" and text = "hello <script>abc</script>world"
		 * 		The resulting string = "hello world"
		 * 
		 * @param String html
		 * @param String tagName	The tag to remove
		 * @return String
		 */
		public static String removeTagAndBody(String html, String tagName) {
			String pattern = "<" + tagName + ".*>.*</" + tagName + "\\s*>";
			return html.replaceAll(pattern, "");
		}
		
		/**
		 * Finds and removes all tags that are not pre-defined as a prioritized tag from the given string.
		 * 
		 * @param String 	html
		 * @return String
		 */
		public static String cleanupTags(String html) {
			// Removes all attributes from opening tags: <tag attr="a"> -> <tag>
			html = html.replaceAll("(<\\w+)[^>]*(>)", "$1$2");
			StringBuffer buffer = new StringBuffer(html);
			int open = buffer.indexOf("<", 0);
			int close = buffer.indexOf(">", open);
			String tag;
			while (open != -1) {
				tag = buffer.substring(open, close+1);
				if (!Constants.Parsing.prioritizedTags.contains(getTagName(tag))) {
					// Remove tag
					buffer.replace(open, close+1, " ");
					open = buffer.indexOf("<", open);
				} else {
					// Pad the tags with spaces to ensure that tags get separated from words
					// 		"word<tag>word" -> "word <tag> word"
					buffer.insert(close+1, " ");
					buffer.insert(open, " ");
					open = buffer.indexOf("<", close);
				}
				close = buffer.indexOf(">", open);
			}
			return buffer.toString();
		}
		
		/**
		 * Returns the tag name. If not given a valid tag, returns an empty string literal.
		 * 
		 * @param String tag	The tag to identify. (Expected inputs: <tag>, </tag>, <tag/>, <tag attribute="a">)
		 * @return String
		 */
		public static String getTagName(String tag) {
			if (!tag.startsWith("<") && !tag.endsWith(">")) {
				throw new IllegalArgumentException();
			}
			return tag.replaceAll("</?(\\w+)[^>]*>", "$1");
		}
		
		/**
		 * Returns true if the given text is a valid tag, else returns false.
		 * 
		 * @param String text
		 * @return boolean
		 */
		public static boolean isTag(String text) {
			return text.startsWith("<") && text.endsWith(">");
		}
		
		/**
		 * Returns true if the given word is an opening tag, else returns false.
		 * 
		 * @param String tag
		 * @return boolean
		 */
		public static boolean isOpeningTag(String tag) {
			return isTag(tag) && !tag.startsWith("</");
		}
		
		/**
		 *  Parse a string of html, extract links
		 * @param String html
		 * @return a hash set of url links in the html
		 */
		public static HashSet<String> parseUrl(String html){
			 UrlDetector parser = new UrlDetector(html, UrlDetectorOptions.Default);
			 HashSet<String> links=new HashSet<String>();
			    List<Url> found = parser.detect();
			    for(Url url : found) {
					links.add(url.getFullUrl());
				}
			    return links;
		}
		
	}
}
