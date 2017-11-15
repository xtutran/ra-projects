package com.xttran.core;

import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 * This class implements the un-supervised clustering algorithm.
 */
public class Learner {

    // Attributes

    private static Logger LOG = Logger.getLogger(ClusteringApp.class);
    // These attributes are only set when the method learn() is called.
    private DataSet dataSet;
    private int numFeats;
    private int[] numFeatVals;
    private int numDocs;
    // parameters for learning
    private int numBurnInIterations;
    private int numSamples;
    private int stepSize;
    private int numInitClusters;
    private double alpha;
    private double beta;
    private double gamma;
    private boolean numClustersIsFixed;

    // Generally we assume that the number of clusters is not fixed but
    // may change during sampling. We therefore keep track of the clusters
    // in the following way:
    // 1. Whenever a new cluster is created, a new numerical label is used //    for that cluster. We never re-use any cluster label.
    // 2. Based on the order they are created, clusters form a sequence. 
    //    We use the ArrayList called activeClusters to store the currently
    //    active clusters, i.e. clusters with a non-zero size.
    // 3. Each active cluster has an index in the ArrayList activeClusters.
    //    We use the HashMap clusterLabelToIndex to store the mapping from
    //    cluster labels to cluster indices.
    // This is the numerical label to be used for the next new cluster.
    private int nextClusterLabel;

    // This ArrayList stores the indices of the active clusters.
    private List<Integer> activeClusters;

    // This HashMap stores the mapping from a cluster label to a cluster
    // index. Only active (i.e. non-empty) clusters are recordered here.
    private Map<Integer, Integer> clusterLabelToIndex;

    // We keep the following counters for Gibbs sampling.

    // For each cluster, we count the number of instances from each document
    // assigned to that cluster.
    // cCounts.get(i)[j] stores the number of elements from doc j in the 
    // i-th cluster. Here i is a cluster index and j is a doc index.
    private List<int[]> cCounts;

    // For each cluster, each feature and each feature value for that 
    // feature, we count the number of instances with that feature value
    // and assigned to that cluster.
    private List<int[][]> fCounts;

    // This array stores the probability for sampling.
    private double[] prob;
    private double initLogFeatProb;

    // This ArrayList stores the collected samples. 
    // samples.get(i)[j] stores the assigned cluster label for the j-th
    // instance in the i-th sample.
    private List<int[]> samples;

    // A Random object for random number generation.
    private Random rand;
    // We use a particular seed to create rand for debugging.
    //private static final int SEED = 0;

    // Constructor
    public Learner(int numBurnInIterations, int numSamples, int stepSize, int numInitClusters, boolean numClustersIsFixed, double alpha, double beta, double gamma) {
        this.numBurnInIterations = numBurnInIterations;
        this.numSamples = numSamples;
        this.stepSize = stepSize;
        this.numInitClusters = numInitClusters;

        this.numClustersIsFixed = numClustersIsFixed;

        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    // This method performs unsupervised clustering on the data set and
    // assigns labels to the instances in the data set.
    public void learn(DataSet dataSet) {
        this.dataSet = dataSet;

        // Initialization
        init();

        // Burn-in period
        for (int i = 1; i <= numBurnInIterations; i++) {
            sample();
            if (i % 100 == 0) {
                LOG.info(i + " iterations finished");
            }
        }

        LOG.info("burn-in period finished");

        // Sample collection
        samples = new ArrayList<int[]>();

        for (int i = 1; i <= numSamples; i++) {
            for (int j = 0; j < stepSize; j++) {
                sample();
            }
            collectSample();
            LOG.info(i + " samples collected");
        }

        // Label assignment
        assignFinalLabels();
    }

    private void init() {

        // Collect some information from the data set.
        DataSpec dataSpec = dataSet.getDataSpec();
        numFeats = dataSpec.getFeatureAlphabet().size();
        numFeatVals = new int[numFeats];
        for (int i = 0; i < numFeats; i++) {
            numFeatVals[i] = dataSpec.getFeatureValueAlphabet(i).size();
        }
        numDocs = dataSpec.getDocumentAlphabet().size();

        // Initialize the Random object.
        //rand = new Random(SEED);
        rand = new Random();

        // Randomly assign a cluster label to each instance.
        // The cluster label is between 0 and (numInitClusters - 1).
        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);
            inst.setY(rand.nextInt(numInitClusters));
        }

        // Initialize the counters.
        initCounters();

        // Initialize the probability array. 
        // Since we may allow new clusters, the size of this array is one
        // more than the current number of active clusters.
        prob = new double[activeClusters.size() + 1];

        if (!numClustersIsFixed) {
            initLogFeatProb = 0.0;
            for (int featIdx = 0; featIdx < numFeats; featIdx++) {
                initLogFeatProb += Math.log(1.0 / numFeatVals[featIdx]);
            }
        }
    }

    private void initCounters() {

        // Create the counter objects.
        cCounts = new ArrayList<int[]>();
        fCounts = new ArrayList<int[][]>();

        activeClusters = new ArrayList<Integer>();
        clusterLabelToIndex = new HashMap<Integer, Integer>();

        // Initialize all counters to zero.
        // Also set up the cluster label-index mapping.
        for (int i = 0; i < numInitClusters; i++) {

            // Initialize the cluster counters.
            // The last element in the array stores the sum.
            int[] counts1 = createNewCCounts();
            cCounts.add(counts1);

            // Initialize the feature counters.
            int[][] counts2 = createNewFCounts();
            fCounts.add(counts2);

            // Set up the cluster mapping.
            activeClusters.add(i);
            clusterLabelToIndex.put(i, i);
        }

        nextClusterLabel = numInitClusters + 1;

        // Based on the initially assigned cluster labels, increase the
        // counters.
        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);
            int cLabel = inst.getY();
            int cIndex = clusterLabelToIndex.get(cLabel).intValue();
            int docIdx = inst.getDoc();

            int[] counts1 = cCounts.get(cIndex);
            counts1[docIdx]++;
            counts1[numDocs]++;

            int[][] counts2 = fCounts.get(cIndex);
            for (int j = 0; j < numFeats; j++) {
                int featValIdx = inst.getFeatureValue(j);
                counts2[j][featValIdx]++;
            }
        }

        if (!numClustersIsFixed) {
            removeEmptyClusters();
        }
    }

    private void removeEmptyClusters() {
        int cIdx = 0;
        while (cIdx < activeClusters.size()) {
            int[] counts1 = cCounts.get(cIdx);
            if (counts1[numDocs] == 0) {
                clusterLabelToIndex.remove(activeClusters.get(cIdx));
                activeClusters.remove(cIdx);
                for (int cIdx2 = cIdx; cIdx2 < activeClusters.size(); cIdx2++) {
                    clusterLabelToIndex.put(activeClusters.get(cIdx2), cIdx2);
                }
                cCounts.remove(cIdx);
                fCounts.remove(cIdx);
            } else {
                cIdx++;
            }
        }
    }

    private int[] createNewCCounts() {
        int[] counts = new int[numDocs + 1];
        for (int i = 0; i < numDocs + 1; i++) {
            counts[i] = 0;
        }
        return counts;
    }

    /**
     * This method creates and returns a 2-dimensional int array that can be 
     * used to store the counts of different feature values for each feature
     * dimension. All counts are set to 0.
     */
    private int[][] createNewFCounts() {
        int[][] counts = new int[numFeats][];
        for (int i = 0; i < numFeats; i++) {
            counts[i] = new int[numFeatVals[i]];
            for (int j = 0; j < numFeatVals[i]; j++) {
                counts[i][j] = 0;
            }
        }
        return counts;
    }

    private void sample() {
        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);
            decreaseCounters(inst);
            inst.setY(sampleClusterLabel(inst));
            increaseCounters(inst);
        }
    }

    private int sampleClusterLabel(Instance inst) {

        int numActiveClusters = activeClusters.size();

        for (int i = 0; i < prob.length; i++) {
            prob[i] = 0.0;
        }

        for (int cIdx = 0; cIdx < numActiveClusters; cIdx++) {

            int[] counts1 = cCounts.get(cIdx);
            int count1 = counts1[numDocs];

            int[][] counts2 = fCounts.get(cIdx);

            for (int featIdx = 0; featIdx < numFeats; featIdx++) {
                int featValIdx = inst.getFeatureValue(featIdx);
                int count2 = counts2[featIdx][featValIdx];

                prob[cIdx] += Math.log((count2 + beta) / (count1 + beta * numFeatVals[featIdx]));
            }

            if (numClustersIsFixed) {
                prob[cIdx] += Math.log(count1 + alpha / numActiveClusters);
            } else {
                int docIdx = inst.getDoc();
                int count3 = counts1[docIdx];
                prob[cIdx] += Math.log((count1 - count3) + gamma * count3);
            }
            //DEBUG
            //System.out.println(prob[cIdx]);
        }

        if (!numClustersIsFixed) {
            prob[numActiveClusters] += initLogFeatProb;
            prob[numActiveClusters] += Math.log(alpha);
        }

        // Adjust the prob values to avoid underflow
        double max = -Double.MAX_VALUE;
        for (int cIdx = 0; cIdx < numActiveClusters; cIdx++) {
            if (prob[cIdx] > max) {
                max = prob[cIdx];
            }
        }
        if (!numClustersIsFixed) {
            if (prob[numActiveClusters] > max) {
                max = prob[numActiveClusters];
            }
        }

        // Convert log probabilities to raw probabilities.
        for (int cIdx = 0; cIdx < numActiveClusters; cIdx++) {
            prob[cIdx] = Math.exp(prob[cIdx] - max);
        }
        if (!numClustersIsFixed) {
            prob[numActiveClusters] = Math.exp(prob[numActiveClusters] - max);
        }

        // Sample
        for (int cIdx = 1; cIdx < numActiveClusters; cIdx++) {
            prob[cIdx] += prob[cIdx - 1];
        }
        if (!numClustersIsFixed) {
            prob[numActiveClusters] += prob[numActiveClusters - 1];
        }

        double r;
        if (numClustersIsFixed) {
            r = rand.nextDouble() * prob[numActiveClusters - 1];
        } else {
            r = rand.nextDouble() * prob[numActiveClusters];
        }

        int chosenCIdx = -1;
        for (int cIdx = 0; cIdx < numActiveClusters; cIdx++) {
            if (r <= prob[cIdx]) {
                chosenCIdx = cIdx;
                break;
            }
        }

        if (!numClustersIsFixed) {
            if (chosenCIdx == -1) {
                chosenCIdx = numActiveClusters;
                clusterLabelToIndex.put(nextClusterLabel, chosenCIdx);
                activeClusters.add(nextClusterLabel);
                nextClusterLabel++;

                cCounts.add(createNewCCounts());
                fCounts.add(createNewFCounts());

                prob = new double[activeClusters.size() + 1];
            }
        }

        return activeClusters.get(chosenCIdx).intValue();
    }

    private void decreaseCounters(Instance inst) {
        int cLabel = inst.getY();
        int cIndex = clusterLabelToIndex.get(cLabel).intValue();
        int docIdx = inst.getDoc();

        int[] counts1 = cCounts.get(cIndex);
        int[][] counts2 = fCounts.get(cIndex);

        counts1[docIdx]--;
        counts1[numDocs]--;

        for (int featIdx = 0; featIdx < numFeats; featIdx++) {
            int featValIdx = inst.getFeatureValue(featIdx);
            counts2[featIdx][featValIdx]--;
        }

        if (!numClustersIsFixed) {
            if (counts1[numDocs] == 0) {
                clusterLabelToIndex.remove(cLabel);
                activeClusters.remove(cIndex);
                for (int cIdx2 = cIndex; cIdx2 < activeClusters.size(); cIdx2++) {
                    clusterLabelToIndex.put(activeClusters.get(cIdx2), cIdx2);
                }
                cCounts.remove(cIndex);
                fCounts.remove(cIndex);
            }
        }
    }

    private void increaseCounters(Instance inst) {

        int cLabel = inst.getY();
        int cIndex = clusterLabelToIndex.get(cLabel).intValue();
        int docIdx = inst.getDoc();

        int[] counts1 = cCounts.get(cIndex);
        int[][] counts2 = fCounts.get(cIndex);

        counts1[docIdx]++;
        counts1[numDocs]++;

        for (int featIdx = 0; featIdx < numFeats; featIdx++) {
            int featValIdx = inst.getFeatureValue(featIdx);
            counts2[featIdx][featValIdx]++;
        }
    }

    private void collectSample() {
        int[] mySample = new int[dataSet.size()];

        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);
            mySample[i] = inst.getY();
        }

        samples.add(mySample);
    }

    private void assignFinalLabels() {

        int[] counts = new int[nextClusterLabel];

        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);

            for (int cLabel = 0; cLabel < nextClusterLabel; cLabel++) {
                counts[cLabel] = 0;
            }

            for (int[] mySample : samples) {
                counts[mySample[i]]++;
            }

            int max = 0;
            int bestCLabel = -1;
            for (int cLabel = 0; cLabel < nextClusterLabel; cLabel++) {
                if (counts[cLabel] > max) {
                    max = counts[cLabel];
                    bestCLabel = cLabel;
                }
            }

            inst.setY(bestCLabel);
        }
    }
}