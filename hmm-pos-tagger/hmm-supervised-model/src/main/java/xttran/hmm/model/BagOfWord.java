package xttran.hmm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BagOfWord {
    private final Map<String, Double> probability = new HashMap<String, Double>();
    private final Map<String, Integer> vocabulary = new HashMap<String, Integer>();
    // private final Map<String, String[]> dictionary = new HashMap<String,
    // String[]>();
    private int size = 0;

    public void increment(String word) {
        addVocabulary(word, 1);
    }

    protected void addVocabulary(String word, Integer value) {
        Integer count = vocabulary.get(word);
        if (count == null) {
            count = value;
        } else {
            count = count + value;
        }
        vocabulary.put(word, count);
        size++;
    }

    protected void calculateProbability() {
        for (Entry<String, Integer> feature : vocabulary.entrySet()) {
            double prob = 0.0;
            if (size > 0) {
                prob = (double) feature.getValue().intValue() / (double) (size);
            }
            probability.put(feature.getKey(), prob);
        }
    }

    public Integer getWordFrequency(String word) {
        Integer freq = vocabulary.get(word);
        if (freq == null) {
            return 0;
        } else {
            return freq.intValue();
        }
    }

    public boolean containWord(String word) {
        return vocabulary.containsKey(word);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return vocabulary.isEmpty();
    }

    public Map<String, Integer> getVocabulary() {
        return vocabulary;
    }

    public Map<String, Double> getProbability() {
        return probability;
    }
}
