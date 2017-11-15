package xttran.hmm;

import xttran.hmm.util.HMMUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HMMModel {
    public static final String COLUMN_SEP = ",";
    public static final String ROW_SEP = ";";
    private static final HMMModel INSTANCE = new HMMModel();
    private int numOfStates; // number of states
    private String[] states;
    private int numOfWords;
    private String[] bagOfWords;
    private Map<String, Integer> dict;
    private double[][] transitionProbs;
    private double[][] emissionProbs;
    private double[] startProbs;

    private HMMModel() {
    }

    public static HMMModel createModel() {
        return INSTANCE;
    }

    // Initially use random transition and emission matrices
    public void initialize(String[] states, String[] bagOfWords) {

        if (states == null || states.length == 0) {
            throw new IllegalArgumentException("HMM: States must be not null or empty");
        }

        if (bagOfWords == null || bagOfWords.length == 0) {
            throw new IllegalArgumentException("HMM: Bag of words must be not null or empty");
        }

        this.states = states;
        this.numOfStates = states.length;

        this.bagOfWords = bagOfWords;
        this.numOfWords = bagOfWords.length;

        this.dict = new HashMap<String, Integer>();
        for (int i = 0; i < this.numOfWords; i++) {
            this.dict.put(bagOfWords[i], i);
        }

        this.transitionProbs = new double[numOfStates][];
        this.emissionProbs = new double[numOfStates][];
        this.startProbs = HMMUtil.random(numOfStates);

        for (int state = 0; state < numOfStates; state++) {
            this.transitionProbs[state] = HMMUtil.random(numOfStates);
            this.emissionProbs[state] = HMMUtil.random(numOfWords);
        }
    }

    /**
     * Store model into file
     *
     * @param filePath
     */
    public void saveToFile(String filePath) {
        // TODO: save model after train to file
        Properties property = new Properties();
        property.setProperty(MODEL.STATES.name(), HMMUtil.join(states, COLUMN_SEP));
        property.setProperty(MODEL.WORDS.name(), HMMUtil.join(bagOfWords, COLUMN_SEP));
        property.setProperty(MODEL.START.name(), HMMUtil.join(startProbs, COLUMN_SEP));
        property.setProperty(MODEL.TRANS.name(), HMMUtil.join(transitionProbs, COLUMN_SEP, ROW_SEP));
        property.setProperty(MODEL.EMMIS.name(), HMMUtil.join(emissionProbs, COLUMN_SEP, ROW_SEP));

        try {
            property.store(new BufferedWriter(new FileWriter(filePath)), "HMM model");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load model from existing file
     *
     * @param filePath
     */
    public void loadFromFile(String filePath) {

        Properties property = new Properties();
        try {
            property.load(new FileReader(filePath));

            String[] states = property.getProperty(MODEL.STATES.name()).split(COLUMN_SEP);
            String[] words = property.getProperty(MODEL.WORDS.name()).split(COLUMN_SEP);

            String[] startsProp = property.getProperty(MODEL.START.name()).split(COLUMN_SEP);
            double[] starts = new double[startsProp.length];
            for (int i = 0; i < startsProp.length; i++) {
                starts[i] = Double.parseDouble(startsProp[i]);
            }

            double[][] trans = HMMUtil.getProbability(property, MODEL.TRANS.name(), ROW_SEP);
            double[][] emmis = HMMUtil.getProbability(property, MODEL.EMMIS.name(), ROW_SEP);

            load(states, words, trans, emmis, starts);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Load model from internal attributes.
     *
     * @param states
     * @param bagOfWords
     * @param transitionProbs
     * @param emissionProbs
     * @param startProbs
     */
    private void load(String[] states, String[] bagOfWords, double[][] transitionProbs, double[][] emissionProbs,
                      double[] startProbs) {
        initialize(states, bagOfWords);
        setProbabilities(transitionProbs, emissionProbs, startProbs);
    }

    public String[] getStates() {
        return states;
    }

    public int getNumOfStates() {
        return numOfStates;
    }

    public int getNumOfWords() {
        return numOfWords;
    }

    public double[] getStartProbs() {
        return startProbs;
    }

    public void setStartProbs(double[] startProbs) {
        if (this.numOfStates != startProbs.length)
            throw new IllegalArgumentException("HMM: start probability matrix must have rows size = number of states");

        this.startProbs = startProbs;
    }

    public void setProbabilities(double[][] transitionProbs, double[][] emissionProbs, double[] startProbs) {
        if (this.numOfStates != transitionProbs.length)
            throw new IllegalArgumentException("HMM: transition probability matrix must have rows size = number of states");
        if (transitionProbs.length != emissionProbs.length)
            throw new IllegalArgumentException("HMM: transition matrix and emission matrix disagree");
        for (int i = 0; i < transitionProbs.length; i++) {
            if (this.numOfStates != transitionProbs[i].length)
                throw new IllegalArgumentException("HMM: transition matrix non-square");
            if (this.numOfWords != emissionProbs[i].length)
                throw new IllegalArgumentException("HMM: emission matrix must have columns size = number of words");
        }

        this.transitionProbs = transitionProbs;
        this.emissionProbs = emissionProbs;

        setStartProbs(startProbs);
    }

    public double[][] getTransitionProbs() {
        return transitionProbs;
    }

    public double[][] getEmissionProbs() {
        return emissionProbs;
    }

    public String[] getBagOfWords() {
        return bagOfWords;
    }

    public int getIndexOfWord(String word) {

        if (word == null || word.isEmpty()) {
            return -1;
        }

        Integer index = dict.get(word);
        if (index == null) {
            return -1;
        }

        return index.intValue();
    }

    public void print() {
        System.out.println("Starting probs:");
        System.out.println(HMMUtil.join(startProbs, ","));

        System.out.println("Transition probs:");
        System.out.println(HMMUtil.join(transitionProbs, ",", "#"));
        // HMMUtil.print(transitionProbs);

        System.out.println("Emmission probs:");
        // HMMUtil.print(emissionProbs);
        System.out.println(HMMUtil.join(emissionProbs, ",", "#"));
    }

    public static enum MODEL {
        STATES, WORDS, START, TRANS, EMMIS
    }
}
