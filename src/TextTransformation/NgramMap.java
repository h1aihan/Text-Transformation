package TextTransformation;

import java.util.ArrayList;
import java.util.HashMap;

public class NgramMap {
	private HashMap<String, Integer> ngrams;
	
	public NgramMap() {
		ngrams = new HashMap<String, Integer>();
	}
	
	public Boolean contain(String gram) {
		return ngrams.containsKey(gram);
	}
	
	public Boolean contains(ArrayList<String> words) {
		return ngrams.containsKey(String.join(" ", words));
	}

	public void insert(String gram) {
		if (ngrams.containsKey(gram)) ngrams.put(gram, ngrams.get(gram) + 1);
		else ngrams.put(gram, 1);
	}
	
	public void insert(ArrayList<String> words) {
		String ngram = String.join(" ", words);
		if (ngrams.containsKey(ngram)) ngrams.put(ngram, ngrams.get(ngram) + 1);
		else ngrams.put(ngram, 1);
	}
	
	public Boolean remove(String gram) {
		return ngrams.remove(gram) != null;
	}
	
	public Boolean remove(ArrayList<String> words) {
		return ngrams.remove(String.join(" ", words)) != null;
	}
}
