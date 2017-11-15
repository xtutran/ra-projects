package xttran.classifier.model;

import xttran.classifier.util.LineParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClassificationModel extends BagOfWord {
    private final Map<String, DocumentCategory> knowledgeBase = new HashMap<String, DocumentCategory>();
    private final BagOfWord features = new BagOfWord();
    /**
     * refinement parameters
     */
    private double anpha = 1;
    private double beta = 0.5;

    public ClassificationModel() {
    }

    public ClassificationModel(double anpha, double beta) {
        this.anpha = anpha;
        this.beta = beta;
    }

    public void addCategory(Object lineData) {

        List<String> words = new ArrayList<String>();
        String category = LineParser.parse(lineData, words);

        increment(category);

        DocumentCategory docCat = knowledgeBase.get(category);
        if (docCat == null) {
            docCat = new DocumentCategory(category);
            knowledgeBase.put(category, docCat);
        }

        docCat.addWords(words);

        //count total features
        for (Entry<String, Integer> entry : docCat.getVocabulary().entrySet()) {
            features.addVocabulary(entry.getKey(), entry.getValue());
        }

        super.calculateProbability();
    }

    public boolean isEmpty() {
        return knowledgeBase.isEmpty();
    }

    public double getAnpha() {
        return anpha;
    }

    public void setAnpha(double anpha) {
        this.anpha = anpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public Map<String, DocumentCategory> getKnowledgeBase() {
        return knowledgeBase;
    }

    public BagOfWord getFeatures() {
        return features;
    }
}
