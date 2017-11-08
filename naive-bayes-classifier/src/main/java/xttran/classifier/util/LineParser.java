package xttran.classifier.util;

import java.util.Arrays;
import java.util.List;

public class LineParser {

    public static String parse(Object lineData, List<String> storedWords) {
        String line = (String) lineData;
        if (line == null || line.isEmpty()) {
            return null;
        }

        String[] tokens = line.split("\\t");

        if (tokens.length != 2) {
            throw new IllegalArgumentException("line data is wrong format!");
        }
        storedWords.addAll(Arrays.asList(tokens[1].split("\\s")));
        return tokens[0];
    }

}
