package de.hpi.semrecsys.utils;

import java.text.DecimalFormat;

public class StringUtils {

	public static int levensteinDistance(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		// i == 0
		int[] costs = new int[b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw
						: nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}

	public static String capitalize(String input) {
		String output = input.substring(0, 1).toUpperCase() + input.substring(1);
		return output;
	}

	public static String equationToString(Object firstTerm, Object secondTerm, String sign) {
		StringBuilder builder = new StringBuilder();
		builder.append(termToString(firstTerm) + " " + sign + " " + termToString(secondTerm));
		return builder.toString();
	}

	public static String doubleToString(Double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		String simValueString = df.format(value);
		return simValueString;
	}

	public static String termToString(Object term) {
		String result = term.toString();
		if (term instanceof Double) {
			Double doubleVal = (Double) term;
			result = doubleToString(doubleVal);
		}
		return result;
	}

}
