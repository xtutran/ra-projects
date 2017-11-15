package com.xttran.experiment;

import com.xttran.experiment.dto.Tag;
import com.xttran.util.StatisticUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import java.io.*;
import java.util.*;

public class Analyzer {
    private static final Analyzer INSTANCE = new Analyzer();
    private BidiMap trueLabels;
    private Map<Integer, Integer> noClusters;
    private List<Tag> labels;
    //private int K;

    //private String[] sa_tt = {"stime", "etime", "loc", "speaker"} ;
    //private String[] mgmt_tt = {"PersonIn_PersonOut",	"Post",	"PersonOut",	"Org->10", "PersonIn"} ;
    private Map<String, Tag> finalLabels;

    private Analyzer() {
        trueLabels = new TreeBidiMap();
        noClusters = new HashMap<Integer, Integer>();
        //K = 0;
    }

    public static Analyzer getInstance() {
        return INSTANCE;
    }

    /**
     * Compute Precision, Recall and F1 measurement
     *
     * @param file
     */
    private void statistic(String file) {
        trueLabels = new TreeBidiMap();
        noClusters = new HashMap<Integer, Integer>();
        finalLabels = new HashMap<String, Tag>();
        labels = new ArrayList<Tag>();
        //K = 0;
        try {
            File inFile = new File(file);
            if (!inFile.exists()) {
                System.err.println("FILE NOT EXISTS");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length != 5) {
                    System.err.println("ERROR PARSING");
                    System.exit(1);
                }

                if (tokens[1].equalsIgnoreCase("link"))
                    continue;

                int clusterLabel = Integer.parseInt(tokens[0]);
                if (!noClusters.containsKey(clusterLabel)) {
                    noClusters.put(clusterLabel, noClusters.size());
                    //K++ ;
                }

                if (!trueLabels.containsKey(tokens[1])) {
                    trueLabels.put(tokens[1], trueLabels.size());
                }
                int candidateLabel = (Integer) trueLabels.get(tokens[1]);
                double precision = Double.valueOf(tokens[2]);
                double recall = Double.parseDouble(tokens[3]);
                double f1 = Double.parseDouble(tokens[4]);

                labels.add(new Tag(clusterLabel, candidateLabel, precision, recall, f1));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(labels, new Comparator<Tag>() {
            // This is where the sorting happens.
            public int compare(Tag o1, Tag o2) {
                return o1.compareTo(o2);
            }
        });

        greedy(labels);
    }

    private void greedy(List<Tag> list) {
        //break condition of recursive
        if (list.size() == 0)
            return;

        Tag max = list.get(0);
        //String candTag = data.getTrueLabelByValue(max.candidateLabel) ;
        String candTag = trueLabels.getKey(max.candidateLabel).toString();
        finalLabels.put(candTag, max);

        List<Tag> copy = remove(list, max.candidateLabel, max.clusterLabel);
        greedy(copy);
    }

    private List<Tag> remove(List<Tag> lLabels, int candIndex, int clusterIndex) {
        List<Tag> copy = new ArrayList<Tag>(lLabels);

        for (Tag l : lLabels) {
            if (l.clusterLabel == clusterIndex)
                copy.remove(l);
            else if (l.candidateLabel == candIndex) {
                copy.remove(l);
            }
        }

        return copy;
    }

    public void writeOutput(String outputFile, Map<String, Tag> statsData) {
        System.out.println("---------------[FINAL EVALUATION RESULT]-----------------");
        StringBuilder builder = new StringBuilder();
        builder.append("Evaluation result" + "\n");

        int i = 0;

        for (Object key : trueLabels.keySet()) {
            if (!statsData.containsKey(key.toString())) {
                statsData.put(key.toString(), new Tag(0, (Integer) trueLabels.get(key), 0.0, 0.0, 0.0));
            }
        }

        List<String> lstring = new ArrayList<String>();
        for (String key : statsData.keySet()) {
            Tag l = statsData.get(key);
            //builder.append(key + "->" + l.clusterLabel + "\t") ;
            lstring.add(key + "->" + l.clusterLabel);
        }

        builder.append("\n");

        Object[] trueLbs = lstring.toArray();

        Object[][] headers = new Object[][]{
                StatisticUtils.addToStartOfArrays("Evaluation", trueLbs)
        };

        builder.append(StatisticUtils.formatStringByColumn(25, 25, headers));
        //System.out.print(Utils.formatStringByColumn(25, 25, headers)) ;

        //print evaluation's result
        int size = trueLbs.length + 1;
        Object[][] data = new Object[3][size];
        data[0][0] = "Precision";
        data[1][0] = "Recall";
        data[2][0] = "F1";
        i = 1;

        for (String key : finalLabels.keySet()) {
            Tag l = finalLabels.get(key);

            data[0][i] = String.format("%.3f", l.precision);
            data[1][i] = String.format("%.3f", l.recall);
            data[2][i] = String.format("%.3f", l.f1);

            i++;
        }

        builder.append(StatisticUtils.formatStringByColumn(25, 25, data));
        //System.out.print(Utils.formatStringByColumn(25, 25, data));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Do report for all of runtimes
     *
     * @param outputPaths
     */
    public void doReport(List<String> inputPaths, String outputDir) {

        List<Map<String, Tag>> allData = new ArrayList<Map<String, Tag>>();

        for (String outputPath : inputPaths) {
            File runFolder = new File(outputPath);

            //int i = 0, k = 1;

            if (runFolder.isFile()) continue;

            File[] files = runFolder.listFiles();

            for (File f : files) {
                if (!f.getName().endsWith(".stats")) continue;

                statistic(f.getAbsolutePath());
                allData.add(finalLabels);
                writeOutput(f.getAbsolutePath().replaceAll(".stats", ".final"), finalLabels);
            }
        }

        //compute average
        Map<String, Tag> average = new HashMap<String, Tag>();
        for (String key : allData.get(0).keySet()) {
            Tag tg = new Tag();
            for (int j = 0; j < allData.size(); j++) {
                Map<String, Tag> mp = allData.get(j);
                tg.sum(mp.get(key));
            }
            tg.average(allData.size());
            average.put(key, tg);
        }

        writeOutput(outputDir + "/report.txt", average);
    }
}
