package TestTextTransformation;
import TextTransformation.*;
import org.junit.*;
import java.util.List;
import com.linkedin.urls.*;
public class UnitTestHtmlParser {
	private HtmlParser hp1;
	private String htmlsegment1="hello this is a url Linkedin.com";
	@Test
	public void testUrlParser() {
		List<Url> u1=hp1.parserUrl(htmlsegment1);
		 for(Url url : u1) {
		        System.out.println("Scheme: " + url.getScheme());
		        System.out.println("Host: " + url.getHost());
		        System.out.println("Path: " + url.getPath());
		        System.out.println("FullUrl: " + url.getFullUrl());
		    }
	}
}
