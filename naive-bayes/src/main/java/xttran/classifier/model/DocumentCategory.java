package xttran.classifier.model;

import java.util.List;

public class DocumentCategory extends BagOfWord {
    private String category;

    public DocumentCategory(String category) {
        this.category = category;
    }

    public void addWords(List<String> words) {
        for (String word : words) {
            increment(word);
        }

        super.calculateProbability();
    }

    public double getLogLikeLiHood(List<String> unknown, ClassificationModel model) {
        if (unknown == null || model == null) {
            return 0;
        }

        Double categoryProb = model.getProbability().get(category);
        if (categoryProb == null) {
            return 0;
        }

        double logFeatureProduct = 0.0;
        for (String word : unknown) {
            Double prob = getProbability().get(word);

            if (prob == null) {
                prob = 0d;
            }

            Integer features = model.getFeatures().getVocabulary().get(word);
            if (features == null) {
                features = 0;
            }

            double logFeatureProb = Math.log(model.getAnpha() * model.getBeta() +
                    features.intValue() * prob.doubleValue()) - Math.log(model.getAnpha() + features.intValue());

            logFeatureProduct += logFeatureProb;
        }

        return Math.log(categoryProb.doubleValue()) + logFeatureProduct;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
