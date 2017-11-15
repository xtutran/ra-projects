package com.xttran.core;

import org.kohsuke.args4j.Option;

/**
 * Environment arguments
 *
 * @author xttran
 */
public class Arguments {
    @Option(name = "-o", usage = "Sets a output path", required = true, metaVar = "String")
    public String output;
    @Option(name = "-i", usage = "Sets a input file path", required = true, metaVar = "String")
    public String inputFile;
    @Option(name = "-p", usage = "Sets a config file path", required = false, metaVar = "String")
    public String configFile;
    @Option(name = "-r", usage = "Sets a number of runs", required = false, metaVar = "Number")
    public int runs = 1;

}
