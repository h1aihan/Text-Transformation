package TextTransformation;

import java.util.ArrayList;
import java.util.HashMap;

public class NgramMap {
	private HashMap<String, Ngram> ngrams;
	
	public NgramMap() {
		ngrams = new HashMap<String, Ngram>();
	}
	
	public Boolean contain(String gram) {
		return ngrams.containsKey(gram);
	}
	
	public Boolean contains(ArrayList<String> words) {
		return ngrams.containsKey(String.join(" ", words));
	}

	public void insert(String gram) {
		if (ngrams.containsKey(gram)) ngrams.get(gram).increment();
		else ngrams.put(gram, new Ngram(gram));
	}
	
	public void insert(ArrayList<String> words) {
		String ngram = String.join(" ", words);
		if (ngrams.containsKey(ngram)) ngrams.get(ngram).increment();
		else ngrams.put(ngram, new Ngram(words));
	}
	
	public Boolean remove(String gram) {
		return ngrams.remove(gram) != null;
	}
	
	public Boolean remove(ArrayList<String> words) {
		return ngrams.remove(String.join(" ", words)) != null;
	}
}
