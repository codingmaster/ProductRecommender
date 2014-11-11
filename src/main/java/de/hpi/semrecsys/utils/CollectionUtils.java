package de.hpi.semrecsys.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hpi.semrecsys.simentity.WikipageLinksEntitySimilarityCalculator.SimilarityCalculationType;

public class CollectionUtils {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
		return sortByValueDesc(map, -1);
	}

	public static <E, V extends Comparable<? super V>, K> Map<K, V> sortByValueDesc(Map<K, V> map, int limit) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		int counter = 0;
		for (Map.Entry<K, V> entry : list) {
			if (limit > 0 && counter >= limit) {
				break;
			}
			result.put(entry.getKey(), entry.getValue());
			counter++;
		}
		return result;
	}

	public static <E, V extends Comparable<? super V>, K> Map<K, V> sortByValueAsc(Map<K, V> map) {
		return sortByValueAsc(map, -1);
	}

	public static <E, V extends Comparable<? super V>, K> Map<K, V> sortByValueAsc(Map<K, V> map, int limit) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		int counter = 0;
		for (Map.Entry<K, V> entry : list) {
			if (limit > 0 && counter >= limit) {
				break;
			}
			result.put(entry.getKey(), entry.getValue());
			counter++;
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, Integer> calculateSum(Map<K, Integer> map) {
		Map<K, Integer> result = new HashMap<K, Integer>();
		for (Map.Entry<K, Integer> entry : map.entrySet()) {
			K key = entry.getKey();
			Integer value = (Integer) getValue(result, key, Integer.class);
			value += entry.getValue();
			result.put(key, value);
		}

		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, Integer> calculateCount(Map<K, Integer> map) {
		Map<K, Integer> result = new HashMap<K, Integer>();
		for (Map.Entry<K, Integer> entry : map.entrySet()) {
			K key = entry.getKey();
			Integer value = (Integer) getValue(result, key, Integer.class);
			value++;
			result.put(key, value);
		}

		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, Integer> calculateCount(List<K> list) {
		Map<K, Integer> result = new HashMap<K, Integer>();
		for (K key : list) {
			Integer value = (Integer) getValue(result, key, Integer.class);
			value++;
			result.put(key, value);
		}

		return result;
	}

	public static Number getValue(Map<? extends Object, ? extends Number> map, Object key, Class<?> clazz) {
		Number value = map.get(key);
		if (value == null) {
			if (clazz.equals(Double.class)) {
				value = 0.0;
			} else {
				value = 0;
			}
		}
		return value;
	}

	public static String mapToString(Map<? extends Object, ? extends Object> result) {
		StringBuilder builder = new StringBuilder();
		for (Entry<? extends Object, ? extends Object> productEntry : result.entrySet()) {
			builder.append(productEntry.getKey() + "\t" + productEntry.getValue() + "\n");
		}
		return builder.toString();
	}

	public static Map<? extends Object, ? extends Double> merge(Map<? extends Object, ? extends Double> map1,
			Map<? extends Object, ? extends Double> map2) {
		Map<Object, Double> result = new HashMap<Object, Double>();

		Map<? extends Object, ? extends Double> biggerMap = map1.size() > map2.size() ? map1 : map2;
		Map<? extends Object, ? extends Double> smallerMap = map1.size() > map2.size() ? map2 : map1;

		for (Entry<? extends Object, ? extends Double> entry : smallerMap.entrySet()) {
			Object key = entry.getKey();
			Double value = entry.getValue();

			Double value2 = (Double) getValue(biggerMap, key, Double.class);
			value2 += value;
			result.put(key, value2);
			biggerMap.remove(key);
		}
		result.putAll(biggerMap);
		return sortByValueDesc(result);
	}

	// public static String collectionToString(Collection<? extends Object>
	// array){
	// StringBuilder builder = new StringBuilder();
	// for(Object obj : array){
	// builder.append(obj);
	// }
	// return builder.toString();
	// }

	public static String collectionToString(Collection<? extends Object> array) {
		String string = "'" + Arrays.toString(array.toArray()).replaceAll("\\[|\\]", "").replaceAll(", ", "','") + "'";
		return string;
	}

	public static Map<? extends Object, ? extends Object> transformSetToMap(Set<? extends Object> objects) {
		Map<Object, Object> map = new HashMap<Object, Object>(objects.size());
		for (Object obj : objects) {
			map.put(obj, obj);
		}
		return map;
	}

	public static Integer getIntersectionSize(Collection<? extends Object> list1, Collection<? extends Object> list2) {
		return getIntersection(list1, list2).size();
	}

	public static Collection<? extends Object> getIntersection(Collection<? extends Object> list1,
			Collection<? extends Object> list2) {
		HashSet<?> hashSet = new HashSet<Object>(list1);
		Collection<?> intersection = hashSet;
		intersection.retainAll(list2);
		return intersection;
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends Object> getUnion(Collection<? extends Object> list1,
			Collection<? extends Object> list2) {
		@SuppressWarnings("rawtypes")
		Collection union = new HashSet(list1);
		union.addAll(list2);
		return union;
	}

	public static Double calculateSimilarity(Collection<? extends Object> list1, Collection<? extends Object> list2,
			SimilarityCalculationType calculationType) {
		Double similarity = 0.0;
		if (list1.isEmpty() || list2.isEmpty()) {
			return 0.0;
		}
		switch (calculationType) {
		case dice:
			similarity = CollectionUtils.calculateDice(list1, list2);
			break;
		case jaccard:
			similarity = CollectionUtils.calculateJaccard(list1, list2);
			break;
		case overlapp:
			similarity = CollectionUtils.calculateOverlap(list1, list2);
			break;
		}
		return similarity;
	}

	private static Double calculateJaccard(Collection<? extends Object> list1, Collection<? extends Object> list2) {
		Collection<? extends Object> intersection = CollectionUtils.getIntersection(list1, list2);
		int divident = list1.size() + list2.size() - intersection.size();
		Double coefficient = (double) ((double) intersection.size() / divident);
		return coefficient;
	}

	/**
	 * Dice coefficient
	 * 
	 * @param list1
	 * @param list2
	 * @return d(list1, list2) = 2 * (double)(intersection.size() /
	 *         (list1.size() + list2.size()));
	 */
	private static Double calculateDice(Collection<? extends Object> list1, Collection<? extends Object> list2) {
		Collection<? extends Object> intersection = CollectionUtils.getIntersection(list1, list2);
		int divident = list1.size() + list2.size();
		Double coefficient = 2 * (double) ((double) intersection.size() / divident);
		return coefficient;
	}

	/**
	 * Overlap coefficient
	 * 
	 * @param linksOut
	 * @param linksOut2
	 * @return o(list1, list2) = (double)(intersection.size() /
	 *         Math.min(list1.size(),list2.size()));
	 */
	private static Double calculateOverlap(Collection<? extends Object> linksOut, Collection<? extends Object> linksOut2) {
		Collection<? extends Object> intersection = CollectionUtils.getIntersection(linksOut, linksOut2);
		int divident = Math.min(linksOut.size(), linksOut2.size());
		Double coefficient = 0.0;
		if (divident != 0) {
			coefficient = (double) ((double) intersection.size() / divident);
		}
		return coefficient;
	}

	/**
	 * Cosine similarity
	 * 
	 * @param map1
	 * @param map2
	 * @return sim(map1, map2) = dotProduct / (map1Magnitude * map2Magnitude);
	 */
	public static Double calculateCosine(Map<Object, Integer> map1, Map<Object, Integer> map2) {
		Integer dotProduct = calculateDotProduct(map1, map2);
		Double map1Magnitude = calculateMagnitude(map1);
		Double map2Magnitude = calculateMagnitude(map2);
		Double coefficient = dotProduct / (map1Magnitude * map2Magnitude);
		return coefficient;

	}

	private static Double calculateMagnitude(Map<Object, Integer> map1) {
		Double sum = 0.0;
		for (Integer value : map1.values()) {
			sum += Math.pow(value, 2);
		}

		return Math.sqrt(sum);
	}

	private static Integer calculateDotProduct(Map<Object, Integer> map1, Map<Object, Integer> map2) {
		Integer sum = 0;
		for (Entry<Object, Integer> entry1 : map1.entrySet()) {
			Integer value1 = entry1.getValue();
			Integer value2 = map2.get(entry1.getKey());
			if (value2 != null) {
				Integer prod = value1 * value2;
				sum += prod;
			}
		}
		return sum;
	}

}