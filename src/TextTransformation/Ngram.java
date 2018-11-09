package TextTransformation;

import java.util.ArrayList;
import java.util.Arrays;

public class Ngram {
	private final int size;
	private int frequency;
	private ArrayList<String> grams;
	
	public Ngram(String word) {
		size = 1;
		frequency = 1;
		grams = new ArrayList<String>(Arrays.asList(word));
	}
	
	public Ngram(ArrayList<String> words) {
		size = words.size();
		frequency = 1;
		grams = new ArrayList<String>(words);
	}
	
	public int getSize() { return size; }
	
	public void decrement() { frequency--; }
	
	public void increment() { frequency++; }
	
	public int getFrequency() { return frequency; }
	
	public String toString() { return String.join(" ", grams);}
	
	public ArrayList<String> getGrams() { return new ArrayList<String>(grams); }
}
