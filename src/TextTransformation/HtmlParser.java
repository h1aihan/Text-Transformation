package TextTransformation;

import java.util.List;
import org.json.*;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

// TODO: Consider making static
//TODO: Document class definition
public class HtmlParser {
	// TODO: Implement
	public List<Url> parserUrl(String html){
		 UrlDetector parser = new UrlDetector(html, UrlDetectorOptions.Default);
		    List<Url> found = parser.detect();

		    for(Url url : found) {
		        System.out.println("Scheme: " + url.getScheme());
		        System.out.println("Host: " + url.getHost());
		        System.out.println("Path: " + url.getPath());
		    }
		    return found; 
	}
	
	public OutputDataStructure parse(JSONObject json) throws Exception {
		// TODO: Implement
		throw new Exception();
	}
	
}
