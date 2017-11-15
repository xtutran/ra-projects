package edu.smu.core;

/**
 * This class stores the specifications of a data set.
 */
public class DataSpec {

    // Attributes

    // This attribute stores the set of true class labels. Each label has a
    // String representation and is mapped to an integer index.
    private Alphabet labels;

    // This attribute stores the set of features used to represent the data.
    // Each feature has a String representation and is mapped to an integer
    // index.
    private Alphabet feats;

    // Each element in this array stores the set of feature values for the
    // corresponding feature. Each feature value has a String representation
    // and is mapped to an integer index.
    private Alphabet[] featVals;

    // This attribute stores the set of documents in the data set. Each
    // document has a String as its unique label, and this String is mapped
    // to an integer index.
    private Alphabet docs;

    // Constructor
    public DataSpec(Alphabet labels, Alphabet feats, Alphabet[] featVals, Alphabet docs) {
        this.labels = labels;
        this.feats = feats;
        this.featVals = featVals;
        this.docs = docs;
    }

    // Getters
    public Alphabet getLabelAlphabet() {
        return labels;
    }

    public Alphabet getFeatureAlphabet() {
        return feats;
    }

    public Alphabet[] getFeatureValueAlphabetArray() {
        return featVals;
    }

    public Alphabet getFeatureValueAlphabet(int featIdx) {
        return featVals[featIdx];
    }

    public Alphabet getDocumentAlphabet() {
        return docs;
    }
    
    /*
    public int getNumberOfFeatures() {
        return feats.size();
    }
    
    public int getNumberOfDocuments() {
        return docs.size();
    }
    
    public int getNumberOfFeatureValues(int featIdx) {
        return featVals[featIdx].size();
    }
    */
    
    /*
    public int[] getNumFeatVals() {
        int[] numFeatVals = new int[feats.size()];
        for(int i = 0; i < feats.size(); i++) {
            numFeatVals[i] = featVals[i].size();
        }
        return numFeatVals;
    }
    */
}