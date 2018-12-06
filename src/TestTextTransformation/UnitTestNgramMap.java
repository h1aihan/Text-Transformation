package TestTextTransformation;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import TextTransformation.NgramMap;

public class UnitTestNgramMap {
	ArrayList<String> oneGram, twoGram, threeGram, fourGram, fiveGram;
	NgramMap testMap;
	
	@Before
	public void SetUp() {
		testMap = new NgramMap();
		oneGram = new ArrayList<String>(Arrays.asList("indigo"));
		twoGram = new ArrayList<String>(Arrays.asList("text", "transformation"));
		threeGram = new ArrayList<String>(Arrays.asList("rensselaer", "polytechnic", "institute"));
		fourGram = new ArrayList<String>(Arrays.asList("adam", "eva", "han", "kaylan"));
		fiveGram = new ArrayList<String>(Arrays.asList("large", "scale", "programming", "and", "testing"));
	}
	
	@Test
	public void testEmptyNgramMap() {
		assertEquals(testMap.getMap().size(), 0);
		assertFalse(testMap.contain("RPI"));
		assertFalse(testMap.contains(oneGram));
		assertFalse(testMap.contains(twoGram));
		assertFalse(testMap.contains(threeGram));
		assertFalse(testMap.contains(fourGram));
		assertFalse(testMap.contains(fiveGram));
		
		assertFalse(testMap.remove("RPI"));
		assertFalse(testMap.remove(oneGram));
		assertFalse(testMap.remove(twoGram));
		assertFalse(testMap.remove(threeGram));
		assertFalse(testMap.remove(fourGram));
		assertFalse(testMap.remove(fiveGram));
	}
	
	@Test
	public void testOneKeyNgramMap() {
		testMap.insert(oneGram);
		
		assertEquals(testMap.getMap().size(), 1);
		assertFalse(testMap.contain("RPI"));
		assertTrue(testMap.contains(oneGram));
		assertFalse(testMap.contains(twoGram));
		assertFalse(testMap.contains(threeGram));
		assertFalse(testMap.contains(fourGram));
		assertFalse(testMap.contains(fiveGram));
		
		assertFalse(testMap.remove("RPI"));
		assertTrue(testMap.remove(oneGram));
		assertFalse(testMap.remove(twoGram));
		assertFalse(testMap.remove(threeGram));
		assertFalse(testMap.remove(fourGram));
		assertFalse(testMap.remove(fiveGram));
		
		assertFalse(testMap.contains(oneGram));
		assertFalse(testMap.remove(threeGram));
	}
	
	
	@Test
	public void testMultipleKeyNgramMap() {
		testMap.insert("RPI");
		testMap.insert(oneGram);
		testMap.insert(twoGram);
		testMap.insert(threeGram);
		testMap.insert(fourGram);
		testMap.insert(fiveGram);
		
		assertEquals(testMap.getMap().size(), 6);
		assertTrue(testMap.contain("RPI"));
		assertTrue(testMap.contains(oneGram));
		assertTrue(testMap.contains(twoGram));
		assertTrue(testMap.contains(threeGram));
		assertTrue(testMap.contains(fourGram));
		assertTrue(testMap.contains(fiveGram));
		
		assertTrue(testMap.remove("RPI"));
		assertTrue(testMap.remove(oneGram));
		assertTrue(testMap.remove(twoGram));
		assertTrue(testMap.remove(threeGram));
		assertTrue(testMap.remove(fourGram));
		assertTrue(testMap.remove(fiveGram));

		assertFalse(testMap.contain("RPI"));
		assertFalse(testMap.contains(oneGram));
		assertFalse(testMap.contains(twoGram));
		assertFalse(testMap.contains(threeGram));
		assertFalse(testMap.contains(fourGram));
		assertFalse(testMap.contains(fiveGram));
		
		assertFalse(testMap.remove("RPI"));
		assertFalse(testMap.remove(oneGram));
		assertFalse(testMap.remove(twoGram));
		assertFalse(testMap.remove(threeGram));
		assertFalse(testMap.remove(fourGram));
		assertFalse(testMap.remove(fiveGram));
	}
}
