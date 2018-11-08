package TextTransformation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.*;


// TODO: Document class definition
public class OutputDataStructure {
	// TODO: Document member variable
	// TODO: Create public accessors
	private HashMap<String, Int> wordGram;
	// TODO: Document member variable
	// Gram type
	// 	1 - Word
	// 	2 - BiGram
	// 	3 - TriGram
	//  ...
	// TODO: Create public accessors
	private int gramN;
	private HashSet<String> links;
	private HashMap<String, String> metaData;
	
	public JSONObject metaDataJSON() throws JSONException  {
		JSONObject metaJSON = new JSONObject();
		metaJSON.put("meta", metaData);
		return metaJSON;
	}
	
	public JSONObject linksJSON() throws JSONException {		
		JSONObject metaJSON = new JSONObject();
		metaJSON.put("links", links);
		return metaJSON;
	}
	
	public JSONObject ngramJSON() throws JSONException {
		JSONObject metaJSON = new JSONObject();
		
		return metaJSON;
	}
	public String metaDataToString() throws JSONException {
		return metaDataJSON().toString();
	}
	
	public String linksToString() throws JSONException {
		return linksJSON().toString();
	}
	public String ngramToString() throws JSONException {
		return ngramJSON().toString();
	}

	public String toString() {
		JSONObject concatenated = new JSONObject();
		try {
			JSONObject metaJSON = metaDataJSON();
			concatenated.put("meta", metaJSON.get("meta"));
			JSONObject linksJSON = linksJSON();
			concatenated.put("links", linksJSON.get("links"));
			JSONObject ngramJSON = ngramJSON();		
			concatenated.put("ngram", ngramJSON.get("ngram"));
		} catch (Exception e) {
			return concatenated.toString();
		}
		return concatenated.toString();
	}

}
