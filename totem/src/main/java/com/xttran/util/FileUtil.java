package com.xttran.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/*
 * A class that provides some utility methods for file handling.
 */
public class FileUtil {

    /**
     * Reads the specified file line by line and stores the lines into the
     * specified <code>List</code> of <code>String</code> objects.
     *
     * @param outputFile  file to write
     * @param lines <code>List</code> to store the lines read
     */
    public static void writeLines(String outputFile, List<String> lines) {

        try {
            File outFile = new File(outputFile);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }

            FileUtils.writeLines(outFile, lines);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}