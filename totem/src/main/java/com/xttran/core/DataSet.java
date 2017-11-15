package com.xttran.core;

/**
 * This class represents a data set.
 */

public class DataSet {

    // Attributes

    // This attribute stores the data specifications.
    private DataSpec dataSpec;

    // This array stores the set of instances in this data set.
    private Instance[] instances;

    // Constructor
    public DataSet(DataSpec dataSpec, Instance[] instances) {
        this.dataSpec = dataSpec;
        this.instances = instances;
    }

    // Getters

    // This method returns the number of instances in this data set.
    public int size() {
        return instances.length;
    }

    // This method returns the instance with the specified index.
    public Instance getInstance(int index) {
        return instances[index];
    }

    public DataSpec getDataSpec() {
        return dataSpec;
    }

}