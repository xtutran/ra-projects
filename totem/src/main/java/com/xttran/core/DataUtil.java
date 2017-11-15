package com.xttran.core;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class provides some utility methods to process data files.
 */
public class DataUtil {

    // This HashSet stores the valid feature Strings.
    private static Set<String> validFeatStrs;

    // This method initializes the set of valid feature Strings.
    private static void initValidFeatStrs() {
        validFeatStrs = new HashSet<String>();
        validFeatStrs.add("cand");
        validFeatStrs.add("head");
        validFeatStrs.add("type");
        validFeatStrs.add("uni_before");
        validFeatStrs.add("uni_after");
        validFeatStrs.add("bi_before");
        validFeatStrs.add("bi_after");
    }

    /**
     * Process input file
     *
     * @param fileName
     * @param checkFeatureValidity
     * @return a DataSet
     * @throws IOException
     */
    public static DataSet processDataFile(File fileName, boolean checkFeatureValidity) throws IOException {

        if (checkFeatureValidity) {
            initValidFeatStrs();
        }

        List<String> lines = FileUtils.readLines(fileName);

        //ArrayList<String> lines = new ArrayList<String>();
        //FileUtil.readLines(fileName, lines);
        //List<String> tokens = new ArrayList<String>();

        // The following Alphabet objects are used to store the mapping
        // between the String representation of items and their indices.
        Alphabet labelAlphabet = new Alphabet();
        Alphabet featureAlphabet = new Alphabet();
        List<Alphabet> featureValueAlphabetList = new ArrayList<Alphabet>();
        Alphabet docAlphabet = new Alphabet();

        // The following ArrayLists store the information of instances.
        // In the end they will be used to create an array of instances.
        List<Integer> instanceLabels = new ArrayList<Integer>();
        List<HashMap<Integer, Integer>> instanceFeatureValues = new ArrayList<HashMap<Integer, Integer>>();
        List<Integer> instanceDocs = new ArrayList<Integer>();
        List<String> instanceIds = new ArrayList<String>();
        List<String> instanceTexts = new ArrayList<String>();

        String[] tokens = null;
        try {

            int i = 0;
            while (i < lines.size()) {

                String line = lines.get(i++);

                tokens = StringUtil.split(line, ' ');

                //tokens.clear();
                //StringUtil.tokenize(line, tokens);

                String docStr = tokens[0];
                int numSents = StringUtil.parseInt(tokens[1], 0);

                int docIdx = docAlphabet.addSymbol(docStr);

                for (int j = 0; j < numSents; j++) {
                    line = lines.get(i++);
                    //tokens.clear();
                    //StringUtil.tokenize(line, tokens);
                    tokens = StringUtil.split(line, ' ');
                    int numCands = Integer.parseInt(tokens[2]);

                    for (int k = 0; k < numCands; k++) {
                        line = lines.get(i++);

                        System.out.println(line);
                        //tokens.clear();
                        tokens = StringUtil.split(line, '\t');
                        //StringUtil.tokenize(line, tokens, "\t");

                        String cand = tokens[0];

                        String[] pair = splitBy(cand, ':', true);

                        String candId = pair[0];
                        cand = pair[1];

                        pair = splitBy(cand, '/', false);

                        String labelStr = pair[1];
                        cand = pair[0];

                        int labelIdx = labelAlphabet.addSymbol(labelStr);

                        HashMap<Integer, Integer> featValMap = new HashMap<Integer, Integer>();

                        boolean hasDuplicate = false;

                        int l = 1;
                        while (l < tokens.length) {
                            String featVal = tokens[l++];

                            if (!featVal.endsWith("1")) {
                                System.err.println("Error: " + line);
                                System.exit(1);
                            }

                            int index = featVal.lastIndexOf(" ");
                            featVal = featVal.substring(0, index);
                            pair = StringUtil.split(featVal, ':');

                            String featStr = pair[0];
                            String featValStr = pair[1];

                            if (checkFeatureValidity) {
                                if (!validFeatStrs.contains(featStr)) {
                                    continue;
                                }
                            }

                            int featIdx = featureAlphabet.addSymbol(featStr);

                            Alphabet featureValueAlphabet = null;
                            if (featIdx < featureValueAlphabetList.size()) {
                                featureValueAlphabet = featureValueAlphabetList.get(featIdx);
                            } else {
                                featureValueAlphabet = new Alphabet();
                                featureValueAlphabetList.add(featIdx, featureValueAlphabet);
                            }
                            int featValIdx = featureValueAlphabet.addSymbol(featValStr);

                            if (featValMap.containsKey(featIdx)) {
                                // System.err.println("Error: duplicate features in '" + line + "'");
                                hasDuplicate = true;
                            }

                            featValMap.put(featIdx, featValIdx);
                        }

                        if (!hasDuplicate) {
                            instanceLabels.add(labelIdx);
                            instanceFeatureValues.add(featValMap);
                            instanceDocs.add(docIdx);
                            instanceIds.add(candId);
                            instanceTexts.add(cand);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception: " + e.getMessage());
            System.err.println("Error: data file!");
            System.exit(1);
        }

        Alphabet[] featureValueAlphabetArray = new Alphabet[featureValueAlphabetList.size()];
        featureValueAlphabetList.toArray(featureValueAlphabetArray);

        // create the "nil" value for some features
        for (HashMap<Integer, Integer> map : instanceFeatureValues) {
            for (int i = 0; i < featureAlphabet.size(); i++) {
                Integer val = map.get(i);
                if (val == null) {
                    val = new Integer(featureValueAlphabetArray[i].addSymbol("nil"));
                    map.put(i, val);
                }
            }
        }

        Instance[] instances = new Instance[instanceLabels.size()];

        for (int i = 0; i < instances.length; i++) {

            int[] values = new int[featureAlphabet.size()];
            for (int j = 0; j < featureAlphabet.size(); j++) {
                values[j] = instanceFeatureValues.get(i).get(j).intValue();
            }

            instances[i] = new Instance(instanceLabels.get(i).intValue(), values, instanceDocs.get(i), instanceIds.get(i), instanceTexts.get(i));

        }

        DataSpec spec = new DataSpec(labelAlphabet, featureAlphabet, featureValueAlphabetArray, docAlphabet);

        DataSet dataSet = new DataSet(spec, instances);

        return dataSet;

    }


    private static String[] splitBy(String str, char delim, boolean front) {
        int ind = -1;

        if (front) {
            ind = str.indexOf(delim);
        } else {
            ind = str.lastIndexOf(delim);
        }

        if (ind == -1) {
            System.err.println("Error: missing '" + delim + "'!");
            System.exit(1);
        }

        String[] result = new String[2];
        result[0] = str.substring(0, ind);
        result[1] = str.substring(ind + 1);

        return result;
    }

}