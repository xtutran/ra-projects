package xttran.classifier.model;

import org.junit.Test;

import xttran.classifier.model.BagOfWord;
import xttran.classifier.util.JsonUtil;

public class TestBagOfWord {
	
	@Test
	public void testAdd() {
		BagOfWord bag = new BagOfWord();
		bag.increment("hello");
		bag.increment("world");
		bag.increment("hello");
		
		bag.calculateProbability();
		
		System.out.println(JsonUtil.serialize(bag));
	}
	
}
