package de.hpi.semrecsys.config;

import java.util.Properties;
/**
 * Properties container for properties/main.properties file
 * @author Michael Wolowyk
 *
 */
public class RecommenderProperties extends Properties {

	private static final long serialVersionUID = 1L;



    public static Double MIN_COLOR_SIMILARITY;
	public static Double MAX_ENTITY_SIMILARITY;
	public static Double MIN_ENTITY_SIMILARITY;
	public static Double MIN_CUSTOM_ENTITY_SIMILARITY;
	public static Integer NUMBER_OF_RESULTS;
    public static Integer MAX_NUMBER_OF_ENTITIES;

	public static Double MAX_PRODUCT_SIMILARITY;
	public static Double MIN_PRODUCT_SIMILARITY;
	public static Double MAX_PRODUCT_FILTER_SIMILARITY;
    public static boolean  CALCULATE_PRECISE = false;

	public RecommenderProperties(Properties props) {
		super(props);
		init(props);
	}

	private void init(Properties properties) {
		MIN_COLOR_SIMILARITY = Double.valueOf(properties.getProperty("MIN_COLOR_SIMILARITY"));
		MAX_ENTITY_SIMILARITY = Double.valueOf(properties.getProperty("MAX_ENTITY_SIMILARITY"));
		MIN_ENTITY_SIMILARITY = Double.valueOf(properties.getProperty("MIN_ENTITY_SIMILARITY"));
		MAX_PRODUCT_SIMILARITY = Double.valueOf(properties.getProperty("MAX_PRODUCT_SIMILARITY"));
        MAX_NUMBER_OF_ENTITIES = Integer.valueOf(properties.getProperty("MAX_NUMBER_OF_ENTITIES"));
		MIN_PRODUCT_SIMILARITY = Double.valueOf(properties.getProperty("MIN_PRODUCT_SIMILARITY"));
		MAX_PRODUCT_FILTER_SIMILARITY = Double.valueOf(properties.getProperty("MAX_PRODUCT_FILTER_SIMILARITY"));
		MIN_CUSTOM_ENTITY_SIMILARITY = Double.valueOf(properties.getProperty("MIN_CUSTOM_ENTITY_SIMILARITY"));
		NUMBER_OF_RESULTS = Integer.valueOf(properties.getProperty("NUMBER_OF_RESULTS"));
        CALCULATE_PRECISE = Boolean.valueOf(properties.getProperty("CALCULATE_PRECISE"));
	}

}
