package de.hpi.semrecsys.utils;

import java.math.BigDecimal;

public class DatatypeHelper {
	public static Double getDoubleValue(Object value) {
		Double count = 0.0;
		if (value.getClass().equals(BigDecimal.class)) {
			BigDecimal bigDecimalCount = (BigDecimal) value;
			count = bigDecimalCount.doubleValue();
		} else {
			count = (Double) value;
		}
		return count;
	}
}
