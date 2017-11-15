package edu.smu.experiment;

import edu.smu.core.Arguments;
import edu.smu.core.ClusteringApp;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.List;

/**
 * Pipe line process
 * 1. do cluster with N run times
 * 2. Analysis cluster
 *
 * @author xttran
 */
public class ExperimentalRunner {

    private static ClusteringApp cluster = ClusteringApp.getInstance();
    private static Analyzer analyzer = Analyzer.getInstance();

    public static void doExperiment(Arguments args) {
        //1. do cluster
        cluster.runApp(args);
        //2. get output paths
        List<String> outputPaths = cluster.getOutputPaths();
        //3. do analysis
        analyzer.doReport(outputPaths, args.output);
    }

    public static void main(String[] args) {
        Arguments argments = new Arguments();
        CmdLineParser parser = new CmdLineParser(argments);
        try {
            parser.parseArgument(args);

            doExperiment(argments);

        } catch (CmdLineException e) {
            //LOG.error(e.getMessage());
            parser.printUsage(System.err);
        }
    }
}
