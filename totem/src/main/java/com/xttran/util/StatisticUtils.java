package com.xttran.util;


import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Xuan-Tu Tran
 */
public class StatisticUtils {

    public static double sdev(double[] numbers) {
        double sum = 0;

        // Taking the average to numbers
        for (int i = 0; i < numbers.length; i++) {
            sum = sum + numbers[i];
        }

        double mean = sum / numbers.length;

        double[] deviations = new double[numbers.length];

        // Taking the deviation of mean from each numbers
        for (int i = 0; i < deviations.length; i++) {
            deviations[i] = numbers[i] - mean;
        }

        double[] squares = new double[numbers.length];

        // getting the squares of deviations
        for (int i = 0; i < squares.length; i++) {
            squares[i] = deviations[i] * deviations[i];
        }

        sum = 0;

        // adding all the squares
        for (int i = 0; i < squares.length; i++) {
            sum = sum + squares[i];
        }

        // dividing the numbers by one less than total numbers
        double result = sum / (numbers.length - 1);

        double standardDeviation = Math.sqrt(result);

        // Taking square root of result gives the
        // standard deviation
        return standardDeviation;
    }

    public static List<String> getByRegex(String text, String regexExp) {
        List<String> matchList = new ArrayList<String>();
        try {
            Pattern regex = Pattern.compile(regexExp);
            Matcher regexMatcher = regex.matcher(text);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group().trim());
            }
        } catch (PatternSyntaxException ex) {
            return null;
            // Syntax error in the regular expression
        }
        return matchList;
    }

    public static String format(double number) {
        String text = String.format("%.3f", number);
        return text;
    }


    public static double round(double number) {
        DecimalFormat df = new DecimalFormat("#.###");
        return Double.valueOf(df.format(number));
    }

    //invert key from value
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValues(final Map<K, V> mapToSort) {
        List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(mapToSort.size());

        entries.addAll(mapToSort.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            public int compare(final Map.Entry<K, V> entry1, final Map.Entry<K, V> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });

        Map<K, V> sortedMap = new LinkedHashMap<K, V>();

        for (Map.Entry<K, V> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static int getMaxIndex(int[] array, int length) {
        int max = array[0];
        int index = 0;
        for (int i = 1; i < length; i++) {
            if (max <= array[i]) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    public static StringBuilder formatStringByColumn(int leftAlign, int rightAlign, Object[][] objs) {
        StringBuilder result = new StringBuilder();
        Formatter formatter = new Formatter(result);
        if (objs.length > 0) {
            int N = objs.length;
            int M = objs[0].length;

            for (int j = 0; j < N; j++) {
                StringBuilder formatString = new StringBuilder();
                String tmp = "%-" + leftAlign + "." + rightAlign + "s";

                for (int i = 0; i < M - 1; i++) {
                    formatString.append(tmp + " ");
                }
                formatString.append(tmp + "\n");
                formatter.format(formatString.toString(), objs[j]);
            }
        }
        formatter.close();
        return result;
    }

    public static Object[] addToStartOfArrays(Object additional, Object[] objs) {
        Object[] extend = new Object[objs.length + 1];
        extend[0] = additional;
        System.arraycopy(objs, 0, extend, 1, objs.length);
        return extend;
    }

}
