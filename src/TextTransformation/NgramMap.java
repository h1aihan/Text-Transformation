package TextTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NgramMap {
	private HashMap<String, Integer> ngrams;
	
	/**
	 * Constructs an NgramMap object.
	 * 
	 * @effects Constructs an empty NgramMap
	 */
	public NgramMap() {
		ngrams = new HashMap<String, Integer>();
	}
	
	/**
	 * Returns true if gram is a key in ngrams.
	 * 
	 * @param gram The word to find
	 * @return true if and if only gram is contained within ngrams
	 */
	public Boolean contain(String gram) {
		return ngrams.containsKey(gram);
	}
	
	/**
	 * Returns true if the concatenated words list is a key in ngrams.
	 * 
	 * @param words The list of words (ngram) to find
	 * @return true if and if only the concatenated words list is contained within ngrams
	 */
	public Boolean contains(ArrayList<String> words) {
		return ngrams.containsKey(String.join(" ", words));
	}

	/**
	 * Increments the frequency of gram in ngrams. If gram isn't contained, gram gets added
	 * to ngrams with a frequency of 1.
	 * 
	 * @param gram The word to insert or update frequency
	 * @modifies ngrams
	 * @effects increases the value of gram in ngrams by 1
	 */
	public void insert(String gram) {
		if (ngrams.containsKey(gram)) ngrams.put(gram, ngrams.get(gram) + 1);
		else ngrams.put(gram, 1);
	}
	
	/**
	 * Increments the frequency of the concatenated words list in ngrams. If gram isn't contained, 
	 * gram gets added to ngrams with a frequency of 1.
	 * 
	 * @param words The list of words (ngram) to insert or update frequency
	 * @modifies ngrams
	 * @effects If contained, increases the value of gram in ngrams by 1, otherwise, added and set to 1
	 */
	public void insert(ArrayList<String> words) {
		String ngram = String.join(" ", words);
		if (ngrams.containsKey(ngram)) ngrams.put(ngram, ngrams.get(ngram) + 1);
		else ngrams.put(ngram, 1);
	}
	
	/**
	 * Returns true if gram can be removed from ngrams
	 * 
	 * @param gram The word to be removed
	 * @modifies ngrams
	 * @effects Removes gram in ngrams if contained
	 * @return true if and if only gram is successfully removed from ngrams
	 */
	public Boolean remove(String gram) {
		return ngrams.remove(gram) != null;
	}
	
	/**
	 * Returns true if the concatenated words list can be removed from ngrams
	 * 
	 * @param gram The list of words (ngrams) to be removed
	 * @modifies ngrams
	 * @effects Removes the concatenated words list in ngrams if contained
	 * @return true if and if only the concatenated words list is successfully removed from ngrams
	 */
	public Boolean remove(ArrayList<String> words) {
		return ngrams.remove(String.join(" ", words)) != null;
	}
	
	/**
	 * Returns the hash map of ngrams
	 * 
	 * @return a hash map copy of ngrams (strings to integers)
	 */
	public HashMap<String, Integer> getMap() {
		return new HashMap<String, Integer>(ngrams);
	}
	
	/**
	 * Returns a hash map that contains only ngrams of length n words.
	 * 
	 * @param n		The number of words the ngrams should consist of.
	 * @return HashMap<String, Integer>
	 */
	public HashMap<String, Integer> getGramsN(int n) {
		HashMap<String, Integer> subMap = new HashMap<String, Integer>();
		Iterator<Entry<String, Integer>> it = ngrams.entrySet().iterator();
		int spaces = 0;
		// Iterate through key-value pairs in ngrams.
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	        // Calculate how many spaces are in the ngram
	        // A gram with x spaces would mean that the gram contains x+1 words 
	        spaces = pair.getKey().length() - pair.getKey().replaceAll(" ", "").length();
	        if (spaces == n - 1) {
	        	subMap.put(pair.getKey(), pair.getValue());
	        }
	    }
		return subMap;
	}
}
