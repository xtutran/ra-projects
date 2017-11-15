package edu.smu.core;

import edu.smu.util.FileUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteringApp {

    private static final ClusteringApp INSTANCE = new ClusteringApp();

    private static final double DEFAULT_ALPHA = 0.1;
    private static final double DEFAULT_BETA = 0.1;
    private static final double DEFAULT_GAMMA = 0.1;
    private static Logger LOG = Logger.getLogger(ClusteringApp.class);
    /**
     * Store list of output for each runtime.
     */
    private final List<String> outputs;

    private ClusteringApp() {
        outputs = new ArrayList<String>();
    }

    public static ClusteringApp getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        Arguments argments = new Arguments();
        CmdLineParser parser = new CmdLineParser(argments);
        ClusteringApp app = ClusteringApp.getInstance();
        try {
            parser.parseArgument(args);

            app.runApp(argments);

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }

    public void runApp(Arguments args) {
        try {
            Param param = loadConfiguration(args.configFile);
            File inputFile = new File(args.inputFile);
            if (!inputFile.exists()) {
                LOG.error("Input file doesn't exist!");
                return;
            }
            DataSet dataSet = DataUtil.processDataFile(inputFile, param.checkFeatureValidity);

            if (args.runs == 1) {

                this.outputs.add(args.output);
                LOG.info("Run default cluster app!");
                doCluster(dataSet, param, args.output, FilenameUtils.getBaseName(args.inputFile));
            } else if (args.runs > 1) {
                for (int i = 0; i < args.runs; i++) {
                    LOG.info("Runtime at: " + i);
                    String out = makeOutputDir(args.output, FilenameUtils.getBaseName(args.inputFile), param, (i));
                    this.outputs.add(out);
                    doCluster(dataSet, param, out, FilenameUtils.getBaseName(args.inputFile));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doCluster(DataSet dataSet, Param param, String outputDir, String outFileName) throws IOException {

        String outputFile = outputDir + File.separator + outFileName + ".output";
        String statFile = outputDir + File.separator + outFileName + ".stats";
        Learner learner = new Learner(param.numBurnInIterations, param.numSamples, param.stepSize, param.numInitClusters, param.numClustersIsFixed, param.alpha, param.beta, param.gamma);

        learner.learn(dataSet);

        Map<Integer, List<Instance>> clusters = getClusters(dataSet);

        outputClusters(clusters, outputFile);
        outputStatistics(dataSet, clusters, statFile);
    }

    private Param loadConfiguration(String confFilePath) {
        PropertiesConfiguration conf = new PropertiesConfiguration();
        Param p = new Param();
        try {
            conf.load(confFilePath);
            p.numBurnInIterations = conf.getInt("burnin", 1000);
            p.numSamples = conf.getInt("samples", 20);
            p.stepSize = conf.getInt("steps", 10);
            p.numInitClusters = conf.getInt("clusters", 5);
            p.numClustersIsFixed = conf.getBoolean("fixedK", false);
            p.alpha = conf.getDouble("alpha", DEFAULT_ALPHA);
            p.beta = conf.getDouble("beta", DEFAULT_BETA);
            p.gamma = conf.getDouble("gama", DEFAULT_GAMMA);
            p.checkFeatureValidity = conf.getBoolean("validate", false);
        } catch (ConfigurationException e) {
            LOG.warn("missing configuration file. App will use default param");
        }
        return p;
    }

    private String makeOutputDir(String outputDir, String outFileName, Param param, int i) {
        String folder = null;
        String tmp;
        if (param.numClustersIsFixed) {
            tmp = "fixed";
            folder = param.numInitClusters + "_" + param.alpha + "_" + param.beta + "/run" + i;
        } else {
            tmp = "nonfixed";
            folder = param.numInitClusters + "_" + param.alpha + "_" + param.beta + "_" + param.gamma + "/run" + i;
        }

        File f = new File(outputDir + "/" + tmp + "/" + outFileName + "/" + folder);
        if (!f.exists()) {
            f.mkdirs();
        }

        return f.getAbsolutePath();
    }

    private Map<Integer, List<Instance>> getClusters(DataSet dataSet) {

        Map<Integer, List<Instance>> clusters = new HashMap<Integer, List<Instance>>();

        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);

            int cLabel = inst.getY();
            if (!clusters.containsKey(cLabel)) {
                clusters.put(cLabel, new ArrayList<Instance>());
            }

            clusters.get(cLabel).add(inst);
        }
        return clusters;
    }

    private void outputClusters(Map<Integer, List<Instance>> clusters, String outputFile) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        Integer[] keyArray = new Integer[clusters.size()];
        clusters.keySet().toArray(keyArray);

        for (int i = 0; i < keyArray.length; i++) {
            lines.add("Cluster " + keyArray[i]);
            for (Instance inst : clusters.get(keyArray[i])) {
                lines.add("\t" + inst.getId() + "\t" + inst.getLabel() + "\t" + inst.getText());
            }
        }

        FileUtil.writeLines(outputFile, lines);
    }

    private void outputStatistics(DataSet dataSet, Map<Integer, List<Instance>> clusters, String outputFile) throws IOException {
        // First we count the number of elements for each true label
        Alphabet labelAlpha = dataSet.getDataSpec().getLabelAlphabet();
        int numLabels = labelAlpha.size();

        int[] trueLabelCounts = new int[numLabels];
        for (int i = 0; i < trueLabelCounts.length; i++) {
            trueLabelCounts[i] = 0;
        }
        for (int i = 0; i < dataSet.size(); i++) {
            Instance inst = dataSet.getInstance(i);
            trueLabelCounts[inst.getLabel()]++;
        }

        // We now compute the prec/rec/f1 of each cluster for each true label
        ArrayList<String> lines = new ArrayList<String>();

        Integer[] keyArray = new Integer[clusters.size()];
        clusters.keySet().toArray(keyArray);

        for (int i = 0; i < keyArray.length; i++) {
            Integer c = keyArray[i];
            List<Instance> cluster = clusters.get(c);

            for (int l = 0; l < numLabels; l++) {
                String labelStr = labelAlpha.getSymbol(l);

				/*
                if (cluster.size() == 0) {
                    lines.add(c + "\t" + labelStr + "\t1\t0\t0");
                    continue;
                }
				 */

                int tp = 0;
                for (Instance inst : cluster) {
                    if (inst.getLabel() == l) {
                        tp++;
                    }
                }

                double prec = (double) tp / cluster.size();
                double rec = (double) tp / trueLabelCounts[l];
                double f1 = 0.0;
                if (tp > 0) {
                    f1 = 2 * prec * rec / (prec + rec);
                }

                lines.add(c + "\t" + labelStr + "\t" + prec + "\t" + rec + "\t" + f1);
            }
        }

        FileUtil.writeLines(outputFile, lines);
    }

    public List<String> getOutputPaths() {
        return outputs;
    }

    /**
     * Parameter for learning
     *
     * @author xttran
     */
    private static class Param {
        private int numBurnInIterations = 1000;
        private int numSamples = 20;
        private int stepSize = 10;
        private int numInitClusters = 5;
        private boolean numClustersIsFixed = false;
        private double alpha = DEFAULT_ALPHA;
        private double beta = DEFAULT_BETA;
        private double gamma = DEFAULT_GAMMA;
        private boolean checkFeatureValidity = false;
    }
}