package xttran.classifier;

import java.io.IOException;

import org.junit.Test;

import xttran.classifier.NaiveBayesClassifier;
import xttran.classifier.model.ClassificationModel;

public class TestNaiveBayesClassifier {
	@Test
	public void testTrain() throws IOException {
		ClassificationModel model = NaiveBayesClassifier.train("src/test/resources/document/r8-train-no-stop.txt");
		
		//System.out.println(JsonUtil.serialize(model));
		
		NaiveBayesClassifier.test(model, "src/test/resources/document/r8-test-no-stop.txt");
	}
}
