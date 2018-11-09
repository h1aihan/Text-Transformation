package TextTransformation;

import java.util.ArrayList;
import java.util.Arrays;

public class NGram {
	private final int size;
	private int frequency;
	private ArrayList<String> grams;
	
	public NGram(String word1) {
		size = 1;
		frequency = 0;
		grams = new ArrayList<String>(Arrays.asList(word1));
	}
	
	public NGram(String word1, String word2) {
		size = 2;
		frequency = 0;
		grams = new ArrayList<String>(Arrays.asList(word1,word2));
	}
	
	public NGram(String word1, String word2, String word3) {
		size = 3;
		frequency = 0;
		grams = new ArrayList<String>(Arrays.asList(word1,word2,word3));
	}
	
	public NGram(String word1, String word2, String word3, String word4) {
		size = 4;
		frequency = 0;
		grams = new ArrayList<String>(Arrays.asList(word1,word2,word3,word4));
	}
	
	public NGram(String word1, String word2, String word3, String word4, String word5) {
		size = 5;
		frequency = 0;
		grams = new ArrayList<String>(Arrays.asList(word1,word2,word3,word4,word5));
	}
	
	public int getSize() { return size; }
	
	public void decrement() { frequency--; }
	
	public void increment() { frequency++; }
	
	public int getFrequency() { return frequency; }
	
	public String toString() { return String.join(" ", grams);}
	
	public ArrayList<String> getGrams() { return new ArrayList<String>(grams); }
}
