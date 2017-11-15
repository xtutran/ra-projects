package com.xttran.core;

/**
 * This class represents a single instance.
 */
public class Instance {

    // Attributes

    // This attribute stores the true class label of this instance (using
    // label index).
    private int label;

    // This attribute stores the feature values (using feature value indices)
    // of this instance.
    private int[] values;

    // This attribute stores the index of the document containing this 
    // instance.
    private int doc;

    // This attribute stores the assignment of cluster label to this instance.
    private int y;

    // The following attributes are not used for learning. 
    // This attribute stores a unique ID for this instance.
    private String id;
    // This attribute stores the textual representation of this instance.
    private String text;

    // Constructor
    public Instance(int label, int[] values, int doc, String id, String text) {
        this.label = label;
        this.values = values;
        this.doc = doc;

        this.id = id;
        this.text = text;

        // -1 indicates that there is no cluster assignment for this 
        // instance.
        y = -1;
    }

    // Getters
    public int getLabel() {
        return label;
    }

    public int[] getFeatureValues() {
        return values;
    }

    public int getFeatureValue(int featIdx) {
        return values[featIdx];
    }

    public int getDoc() {
        return doc;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getY() {
        return y;
    }

    // Setters
    public void setY(int y) {
        this.y = y;
    }
    
    /*
    public String toString() {
        String myText = "Instance[";
        for(int i = 0; i < values.length; i++) {
            myText += values[i] + " ";
        }
        myText += "]";
        return myText;
    }
    
    public String getTextRepresentation(DataSpec dataSpec) {
        Alphabet labelAlpha = dataSpec.getLabels();
        Alphabet featAlpha = dataSpec.getFeats(); 
        Alphabet[] featValAlphas = dataSpec.getFeatVals();
    
        String myText = id + ":" + text;
        String labelStr = labelAlpha.getSymbol(label);
        myText += "/" + labelStr;
        for(int i = 0; i < featAlpha.size(); i++) {
            String featStr = featAlpha.getSymbol(i);
            String valStr = featValAlphas[i].getSymbol(values[i]);
            myText += " " + featStr + ":" + valStr;
        }
        
        return myText;
    
    }
    */
}