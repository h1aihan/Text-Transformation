package TextTransformation;

import java.net.InetSocketAddress;

public final class Constants {

	public static class JSON {
		public static final String linkForwardAddressKey = "linkAnalysis";
		public static final String indexingForwardAddressKey = "indexing";
		public static final String metaDataKey = "meta";
		public static final String linksKey = "links";
		public static final String ngramsKey = "ngrams";
		
	}
	public static class Networking {
		public static final String rootAddress = "/";
		public static final String transformAndReturn = rootAddress + "transformed";
		public static final String transformAndForward = rootAddress + "transform";
		public static final InetSocketAddress socketAddress = new InetSocketAddress(8080);
	}
	public static class StaticText {
		public static String NetworkDefaultError = "Uh oh! Something went wrong...";
		public static String NetworkInvalidRequestError = "It appears that an invalid request was made...";
		public static String NetworkUnableToParseError = "The Parser was unable to parse the object";
		public static String NetworkInavlidForwardAddressError = "Forwarding to a forward address failed";
		public static String NetworkWelcomeMessage = "Hello! Welcome to the Indigo-O Text Transformer!\n"
																+ "\tTry /transform to transform HTML\n";
	}
}
