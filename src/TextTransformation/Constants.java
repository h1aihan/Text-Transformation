package TextTransformation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Constants {

	public static class JSON {
		public static final String linkForwardAddressKey = "linkAnalysis";
		public static final String indexingForwardAddressKey = "indexing";
		public static final String metaDataKey = "meta";
		public static final String linksKey = "links";
		public static final String ngramsKey = "ngrams";
		public static final String htmlInputKey = "raw_html";
		
	}
	public static class Networking {
		public static final String rootAddress = "/";
		public static final String infoAddress = rootAddress + "info";
		public static final String transformAndReturn = rootAddress + "transformed";
		public static final String transformAndForward = rootAddress;
		public static final InetSocketAddress socketAddress = new InetSocketAddress(8080);
	}
	public static class StaticText {
		public static String NetworkDefaultError = "Uh oh! Something went wrong...";
		public static String NetworkInvalidRequestError = "It appears that an invalid request was made...";
		public static String NetworkUnableToParseError = "The Parser was unable to parse the object";
		public static String NetworkInavlidForwardAddressError = "Forwarding to a forward address failed";
		public static String NetworkWelcomeMessage = "Hello! Welcome to the Indigo-O Text Transformer!\n"
																+ "\tTry /transform to transform HTML";
		public static String NetworkDefaultTitle = "transform text with indigo-O";
		public static String NetworkWelcomeMessageHTML = "<HTML><HEAD><TITLE>" + Constants.StaticText.NetworkDefaultTitle + "</TITLE></HEAD>" +
														 "<BODY>" + Constants.StaticText.NetworkWelcomeMessage + "</BODY></HTML>";
	}
	
	public static class Parsing {
		public static final String delimiters = "[\\s\\\".)(!?,;:-]+";
		private static final String[] tagValues = {"title", "h1", "h2", "h3", "h4", "h5", "h6"};
		public static final Set<String> prioritizedTags = new HashSet<>(Arrays.asList(tagValues)); 
	}
	
	public static class StaticCollections {
		private static Set<String> getStopWords() {
			HashSet<String> words = new HashSet<String>();
			try {
				BufferedReader input = new BufferedReader(new FileReader("src/TextTransformation/stopwords.txt"));
				for (String line = input.readLine(); line != null; line = input.readLine()) words.add(line);
				input.close();
			} catch (IOException e) {
			}
			return Collections.unmodifiableSet(words);
		}
		
		public static final Set<String> StopWords = getStopWords();
	}
}
