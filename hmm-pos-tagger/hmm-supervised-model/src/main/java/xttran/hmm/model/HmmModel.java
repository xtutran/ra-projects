package xttran.hmm.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class HmmModel {

	private BagOfWord context = new BagOfWord();
	private BagOfWord transition = new BagOfWord();
	private BagOfWord emission = new BagOfWord();
	private BagOfWord words = new BagOfWord();
	
	public static final String WORD_TAG_SEP = "/";
	public static final String SEQUENCE_SEP = " ";
	public static final String ARROW = "->";
	public static final String START_STATE = "start";
	public static final String END_STATE = "end";
	
	public void train(String trainDataset) throws IOException {
		
		for (Object file : FileUtils.listFiles(new File(trainDataset), new String[]{"pos"}, true)) {
			//System.out.println(file);
			File trainFile = (File) file;
			readData(trainFile.getAbsolutePath());
		}
	}

	public void readData(String trainFile) throws IOException {
		for(Object line : IOUtils.readLines(new FileInputStream(trainFile), "UTF-8")) {
			String previous = START_STATE;
			context.increment(previous);

			String[] sequences = StringUtils.split((String)line, SEQUENCE_SEP);
			for(String sequence : sequences) {
				String[] wordtags = StringUtils.split(sequence, WORD_TAG_SEP);
				
				if(wordtags.length != 2) {
					//System.err.println(sequence);
					continue;
				}
				
				String word = wordtags[0];
				String tag = wordtags[1];
				
				words.increment(word);
				transition.increment(previous + ARROW + tag);
				context.increment(tag);
				emission.increment(tag + ARROW + word);
				previous = tag;
			}
			transition.increment(previous + ARROW + END_STATE);
		}
	}
	
	public boolean isEmpty() {
		return words.isEmpty() || context.isEmpty() || transition.isEmpty() || emission.isEmpty();
	}

	/**
	 * Laplace Smoothed Hidden Markov Model
	 * Calculate Ps(ti)
	 */
	public void calcStartProbs() {
		//starting probs
		double total = 0;
		for(Entry<String, Integer> entry :  context.getVocabulary().entrySet()) {
			total += entry.getValue().intValue();
		}

		for(Entry<String, Integer> entry :  context.getVocabulary().entrySet()) {
			double prob = Math.log(entry.getValue().intValue() + 1) - Math.log(total + context.getVocabulary().size());
			
			context.getProbability().put(entry.getKey(), -prob);
		}
	}

	/**
	 * Laplace Smoothed Hidden Markov Model
	 * Calculates Pe(wj|ti)
	 */
	public double calcEmissionProb(String ti, String wj){
		
		int Ctiwj = emission.getWordFrequency(ti + ARROW + wj);
		int Cti = context.getWordFrequency(ti);
		int wordSize = words.getVocabulary().size();
		
		double prob = Math.log(Ctiwj + 1) - Math.log(Cti + wordSize);
		
		return -prob;
	}

	/**
	 * Laplace Smoothed Hidden Markov Model
	 * Calculates Pt(tj|ti) 
	 */
	public double calcTransitionProb(String ti, String tj){
		
		int Ctitj = transition.getWordFrequency(ti + ARROW + tj);
		int Cti = context.getWordFrequency(ti);
		int tagSize = context.getVocabulary().size();
		
		double prob = Math.log(Ctitj + 1) - Math.log(Cti + tagSize);
		return -prob;
	}
	
	public BagOfWord getContext() {
  	return context;
  }

	public BagOfWord getTransition() {
  	return transition;
  }

	public BagOfWord getEmission() {
  	return emission;
  }

	public BagOfWord getWords() {
  	return words;
  }
}
