package xttran.hmm.util;

import java.util.Properties;

public class HMMUtil {
	/**
	 * parse property file into matrix of probs
	 * 
	 * @param prop
	 * @param propertyKey
	 * @return
	 */
	public static double[][] getProbability(Properties prop, String propertyKey, String sep) {
		String[] probProp = prop.getProperty(propertyKey).split(sep);
		double[][] probs = new double[probProp.length][];
		for (int i = 0; i < probProp.length; i++) {
			String[] colums = probProp[i].split(",");
			probs[i] = new double[colums.length];
			for (int j = 0; j < colums.length; j++) {
				probs[i][j] = Double.parseDouble(colums[j]);
			}
		}
		return probs;
	}

	/**
	 * init random probs
	 * 
	 * @param n
	 * @return
	 */
	public static double[] random(int n) {
		double[] ps = new double[n];
		double sum = 0;
		// Generate random numbers
		for (int i = 0; i < n; i++) {
			ps[i] = Math.random();
			sum += ps[i];
		}
		// Scale to obtain a discrete probability distribution
		for (int i = 0; i < n; i++) {
			ps[i] /= sum;
		}
		return ps;
	}

	/**
	 * divides two doubles. 0 / 0 = 0!
	 * 
	 */
	public static double divide(double n, double d) {
		if (n == 0) {
			return 0;
		} else {
			return n / d;
		}
	}

	public static void print(double[][] probs) {
		if (probs == null || probs.length == 0) {
			System.out.println("[WARNING] null or empty probs");
			return;
		}

		for (int i = 0; i < probs.length; i++) {
			for (int j = 0; j < probs[0].length; j++) {
				System.out.print(probs[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static String join(String[] array, String seperator) {

		if (array == null) {
			return null;
		}

		int length = array.length;

		if (length == 0) {
			return "";
		} else if (length == 1) {
			return array[0];
		}

		int i = 0;
		StringBuilder builder = new StringBuilder(16 * length * 2);
		for (; i < length - 1; i++) {
			builder.append(array[i]);
			builder.append(seperator);
		}
		builder.append(array[i]);

		return builder.toString();
	}

	public static String join(double[] array, String seperator) {
		if (array == null) {
			return null;
		}

		int length = array.length;

		if (length == 0) {
			return "";
		} else if (length == 1) {
			return array[0] + "";
		}

		int i = 0;
		StringBuilder builder = new StringBuilder(16 * length * 2);
		for (; i < length - 1; i++) {
			builder.append(array[i]);
			builder.append(seperator);
		}
		builder.append(array[i]);

		return builder.toString();
	}

	public static String join(double[][] array, String seperator1, String seperator2) {
		if (array == null) {
			return null;
		}

		int length = array.length;
		int i = 0;
		StringBuilder builder = new StringBuilder(16 * length * 2);
		for (; i < length - 1; i++) {
			String row = join(array[i], seperator1);
			builder.append(row);
			builder.append(seperator2);
		}
		builder.append(join(array[i], seperator1));
		return builder.toString();
	}
}
