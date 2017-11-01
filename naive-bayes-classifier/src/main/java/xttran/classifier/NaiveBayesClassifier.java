package xttran.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import xttran.classifier.model.BagOfWord;
import xttran.classifier.model.ClassificationModel;
import xttran.classifier.model.DocumentCategory;
import xttran.classifier.util.JsonUtil;
import xttran.classifier.util.LineParser;

public class NaiveBayesClassifier {
	
	public static void main(String[] args) throws IOException {
		//train model
		ClassificationModel model = train("src/test/resources/document/r8-train-no-stop.txt");
		//test model and output evaluation scores
		test(model, "src/test/resources/document/r8-test-no-stop.txt");
		
		//classify unknown document
		classify(model, "src/test/resources/document/r8-test-no-stop.txt", "src/test/resources/document/r8-test-no-stop.out");
	}

	public static ClassificationModel train(String traindata) throws IOException {
		ClassificationModel model = new ClassificationModel();

		File trainData = new File(traindata);
		if(!trainData.exists() || !trainData.isFile()) {
			throw new IllegalArgumentException("train file doesn't exist or not file");
		}

		for(Object line : IOUtils.readLines(new FileInputStream(trainData), "UTF-8")) {
			model.addCategory(line);
		}

		return model;
	}
	
	public static void writeModelToFile(ClassificationModel model, String outputPath) throws IOException {
		String serialize = JsonUtil.serialize(model);
		IOUtils.write(serialize, new FileOutputStream(outputPath), "UTF-8");
	}
	
	public static ClassificationModel readModelFromFile(String modelPath) throws IOException {
		String json = (String) IOUtils.readLines(new FileInputStream(modelPath), "UTF-8").get(0);
		return JsonUtil.derialize(json, ClassificationModel.class);
	}
	
	public static void test(ClassificationModel model, String testDataset) throws IOException {
		BagOfWord trueClasses = new BagOfWord();
		Map<String, Integer> positiveClasses = new HashMap<String, Integer>();
		Map<String, Integer> relevantClasses = new HashMap<String, Integer>();
		
		InputStream input = new FileInputStream(testDataset);
		for(Object line : IOUtils.readLines(input, "UTF-8")) {
			
			List<String> words = new ArrayList<String>();
			String category = LineParser.parse(line, words);
			
			trueClasses.increment(category);

			String predict = classify(words, model);
			
			Integer count = positiveClasses.get(predict);
			if(count == null) {
				count = 0;
			}
			
			relevantClasses.put(predict, count + 1);
			
			if(category.equalsIgnoreCase(predict)) {
				positiveClasses.put(predict, count + 1);
			}
		}
		
		System.out.println("##########################Evaluation (Precision<tab>Recall<tab>F1)###############################");
		double overallPrec = 0.0;
		double overallRec = 0.0;
		double overallF1 = 0.0;
		for(Entry<String, Integer> entry : trueClasses.getVocabulary().entrySet()) {
			
			String category = entry.getKey();
			Integer trueCount = entry.getValue();
			
			Integer positiveCount = positiveClasses.get(category);
			Integer relevantCount = relevantClasses.get(category);
			if(positiveCount == null) {
				positiveCount = 0;
			}
			
			if(relevantCount == null) {
				relevantCount = 0;
			}
			
			double prec = (double)positiveCount.intValue() / trueCount;
			double rec = (relevantCount.intValue() == 0) ? 0 :(double)positiveCount.intValue() / relevantCount.intValue();
			double f1 = 0.0;
			if (positiveCount.intValue() > 0) {
				f1 = 2 * prec * rec / (prec + rec); 
			}
			
			overallPrec += prec;
			overallRec += rec;
			overallF1 += f1;
			
			String statisticLine = prec + "\t" + rec + "\t" + f1;
			System.out.println(category + ": " + statisticLine);
		}
		
		overallPrec = ((double)overallPrec) / trueClasses.getVocabulary().size();
		overallRec = ((double)overallRec) / trueClasses.getVocabulary().size();
		overallF1 = ((double)overallF1) / trueClasses.getVocabulary().size();
		
		System.out.println("Overall: " + overallPrec + "\t" + overallRec + "\t" + overallF1);
		
		input.close();
	}

	public static void classify(ClassificationModel model, String inputFile, String outputFile) throws IOException {

		InputStream input = new FileInputStream(inputFile);
		OutputStream output = new FileOutputStream(outputFile);
		
		List<String> predictedLine = new ArrayList<String>();
		for(Object line : IOUtils.readLines(input, "UTF-8")) {
			String[] words = ((String)line).split("\\s");

			String predict = classify(Arrays.asList(words), model);
			predictedLine.add(predict + "\t" + line);
		}
		
		IOUtils.write(StringUtils.join(predictedLine.toArray(new String[0]), "\n"), output, "UTF-8");
		
		input.close();
		output.close();
	}

	public static String classify(List<String> words, ClassificationModel model) {
		if(model == null || model.isEmpty()) {
			throw new IllegalArgumentException("train model must be not null or empty");
		}

		Map<String, DocumentCategory> knowledgeBases = model.getKnowledgeBase();
		String result = null;
		double argmax = Double.NEGATIVE_INFINITY;
		for (Entry<String, DocumentCategory> knowledgeBase : knowledgeBases.entrySet()) {
			double likelihood = knowledgeBase.getValue().getLogLikeLiHood(words, model);

			if(likelihood >= argmax) {
				argmax = likelihood;
				result = knowledgeBase.getKey();
			}
		}
		//System.out.println("argmax = " + argmax);
		return result;
	}
}
