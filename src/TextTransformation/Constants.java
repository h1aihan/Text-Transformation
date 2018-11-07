package TextTransformation;

import java.net.InetSocketAddress;

public final class Constants {

	public static class JSONKeys {
		public static final String linkForwardAddress = "linkAnalysis";
		public static final String indexingForwardAddress = "indexing";
		public static final String metaData = "meta";
		
	}
	public static class Networking {
		public static final InetSocketAddress socketAddress = new InetSocketAddress(8080);
	}
	public static class ErrorMessage {
		public static String NetworkDefault = "Uh oh! Something went wrong...";
		public static String NetworkInvalidRequest = "It appears that an invalid request was made...";
	}
}
