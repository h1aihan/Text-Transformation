package TextTransformation;
import java.util.HashMap;
import java.util.HashSet;
import org.json.*;


// TODO: Document class definition
public class OutputDataStructure {
	// TODO: Document member variable
	// TODO: Create public accessors
	private HashMap<String, NgramMap> wordGrams;
	// TODO: Document member variable
	// Gram type
	// 	1 - Word
	// 	2 - BiGram
	// 	3 - TriGram
	//  ...
	// TODO: Create public accessors
	private HashSet<String> links;
	private HashMap<String, String> metaData;
	
	public OutputDataStructure(HashMap<String, NgramMap> ngrams, HashSet<String> links) {
		this.wordGrams = ngrams;
		this.links=links;
	}
	
	public JSONObject getMetaDataJSON() throws JSONException  {
		return new JSONObject(metaData);
	}
	
	public JSONObject getLinksJSON() throws JSONException {		
		return new JSONObject(links);
	}
	
	public JSONObject getNGramJSON() throws JSONException {
		JSONObject ngramJSON = new JSONObject();
		for (String key : wordGrams.keySet()) {
			JSONObject ngramGroup = new JSONObject();
			for (int n=1; n <= 5; n++) {
				ngramGroup.put(n+"grams", wordGrams.get(key).getGramsN(n));
			}
			ngramJSON.put(key, ngramGroup);
		}
		return ngramJSON;
	}
	
	public String metaDataToString() throws JSONException {
		return getMetaDataJSON().toString();
	}
	
	public String linksToString() throws JSONException {
		return getLinksJSON().toString();
	}
	public String ngramToString() throws JSONException {
		return getNGramJSON().toString();
	}

	public String toString() {
		JSONObject concatenated = new JSONObject();
		try {
			concatenated.put("meta", getMetaDataJSON());
		} catch (Exception e) {}
		try {
			concatenated.put("links", getLinksJSON());
		} catch (Exception e) {}
		try {
			concatenated.put("ngram", getNGramJSON());
		} catch (Exception e) {}
		
		return concatenated.toString();
	}

}

