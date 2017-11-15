package com.xttran.core;

import java.util.List;
import java.util.StringTokenizer;

/**
 * A class that provides some utility methods for <code>String</code>
 * manipulation.
 */
public class StringUtil {

    /**
     * Splits the given <code>String</code> into tokens.
     *
     * @param line   <code>String</code> to be tokenized
     * @param tokens <code>ArrayList</code> to store the tokens
     */
    public static void tokenize(String line, List<String> tokens) {
        StringTokenizer strTok = new StringTokenizer(line);
        while (strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            tokens.add(token);
        }
    }

    public static void tokenize(String line, List<String> tokens, String delim) {
        StringTokenizer strTok = new StringTokenizer(line, delim);
        while (strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            tokens.add(token);
        }
    }

    public static String[] split(String s, char delimeter) {
        int count = 1;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == delimeter)
                count++;

        String[] array = new String[count];

        int a = -1;
        int b = 0;

        for (int i = 0; i < count; i++) {

            while (b < s.length() && s.charAt(b) != delimeter)
                b++;

            array[i] = s.substring(a + 1, b);
            a = b;
            b++;
        }

        return array;
    }

    public static int parseInt(String value, int defaultVal) {

        if (value == null || value.length() == 0) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException pe) {
            return defaultVal;
        }
    }

}