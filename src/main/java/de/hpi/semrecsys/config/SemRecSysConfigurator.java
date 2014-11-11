package de.hpi.semrecsys.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import opennlp.tools.util.InvalidFormatException;

import org.apache.log4j.Logger;
import org.hibernate.exception.SQLGrammarException;

import virtuoso.jdbc3.VirtuosoDataSource;

import com.google.gson.Gson;

import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;
import de.hpi.semrecsys.main.Main;
import de.hpi.semrecsys.spotlight.SpotlightConnector;
import de.hpi.semrecsys.utils.FileUtils;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.utils.TextExtractor;

/**
 * Main configuration class. Singleton
 * 
 * @author Michael Wolowyk
 * 
 */
public class SemRecSysConfigurator {

	private static SemRecSysConfigurator configurator;
	static String virtuosoBaseGraph;
	static String virtuosoHostUrl;
	static Logger log = Logger.getLogger(SemRecSysConfigurator.class);

	/**
	 * Properties paths
	 */
	static String propertiesDirPath;
	SemRecSysConfiguratorData data = new SemRecSysConfiguratorData("virtuoso.properties", "sparql/sparql.xml",
			"color_list", "custom");
	private String srcSchema;
	private String targetSchema;
	private String virtuosoSparqlEndpoint;
	private String dbpediaNamespace;
	private static Customer customer;
	private static ConfiguratorParameters parameters;

	/**
	 * Customer name
	 * 
	 * @author Michael Wolowyk
	 * 
	 */
	public enum Customer {
		melovely, naturideen, naturideen2;
	}

	/**
	 * returns default configurator singleton or creates a new one if it doesn't
	 * exists
	 * 
	 * @param customer
	 * @param dbInitMode
	 *            special configurations for initialisation purposes
	 * @return default configurator singleton
	 */
	public static SemRecSysConfigurator getDefaultConfigurator(Customer customer, boolean dbInitMode) {
		parameters = new ConfiguratorParameters(customer, LanguageCode.DE, null);
		parameters.dbInitMode = dbInitMode;
		return getDefaultConfigurator(parameters);
	}

	/**
	 * returns default configurator singleton or creates a new one if it doesn't
	 * exists
	 * 
	 * @param customer
	 * @return default configurator singleton
	 */
	public static SemRecSysConfigurator getDefaultConfigurator(Customer customer) {
		return getDefaultConfigurator(customer, LanguageCode.DE);
	}

	/**
	 * returns default configurator singleton for special language or creates a
	 * new one if it doesn't exists
	 * 
	 * @param customer
	 * @param languageCode
	 * @return default configurator singleton
	 */
	public static SemRecSysConfigurator getDefaultConfigurator(Customer customer, LanguageCode languageCode) {
		propertiesDirPath = FileUtils.readTextFromFile(Main.confPath);
		if (!propertiesDirPath.endsWith("/")) {
			propertiesDirPath += "/";
		}
		parameters = new ConfiguratorParameters(customer, languageCode, propertiesDirPath);
		return getDefaultConfigurator(parameters);
	}

	/**
	 * returns default configurator singleton or creates a new one if it doesn't
	 * exists
	 * 
	 * @param parameters
	 *            configuration parameters
	 * @return defualt configurator
	 */
	public static SemRecSysConfigurator getDefaultConfigurator(ConfiguratorParameters parameters) {
		if (configurator == null) {
			SemRecSysConfigurator.parameters = parameters;
			propertiesDirPath = initPropertiesPath(parameters);
			SemRecSysConfigurator.customer = parameters.customer;
			configurator = new SemRecSysConfigurator();
			log.info("Properties path is set to: " + propertiesDirPath);
			log.info("Customer: " + parameters.customer);
			log.info("Language: " + parameters.languageCode);
		}
		return configurator;
	}

	private SemRecSysConfigurator(ConfiguratorParameters parameters) {
		SemRecSysConfigurator.parameters = parameters;
		propertiesDirPath = initPropertiesPath(parameters);
		SemRecSysConfigurator.customer = parameters.customer;

		init();
		log.info("Properties path is set to: " + propertiesDirPath);
		log.info("Customer: " + parameters.customer);
		log.info("Language: " + parameters.languageCode);
	}

	private SemRecSysConfigurator() {
		init();
	}

	private static String initPropertiesPath(ConfiguratorParameters parameterObject) {
		String propertiesDiString = "";
		if (parameterObject.propsDirPath != null) {
			propertiesDiString = parameterObject.propsDirPath;
		} else {
			propertiesDiString = FileUtils.readTextFromFile(Main.confPath);
		}
		if (!propertiesDiString.endsWith("/")) {
			propertiesDiString += "/";
		}
		return propertiesDiString;
	}


	public static String getCustomerPropertiesPath() {
		return propertiesDirPath + customer.name();
	}


	public static String getPropertiesDirPath() {
		return propertiesDirPath;
	}


	public CustomAttributeSimilarityCreator getCustomAttributeSimilarityCreator() {
		if (data.customAttributeSimilarityCreator == null) {
			data.customAttributeSimilarityCreator = new CustomAttributeSimilarityCreator(getCustomerPropertiesPath()
					+ "/attributes");
		}
		return data.customAttributeSimilarityCreator;
	}


	public Properties getSparqlProperties() {
		return data.sparqlProperties;
	}

	public Namespacer getNamespacer() {
		return data.namespacer;
	}

	public String getSpotlightUrl() {
		return data.spotlightUrl;
	}


	public RecommenderProperties getRecommenderProperties() {
		return data.recommenderProperties;
	}

	public TextExtractor getTextExtractor() {
		return data.textExtractor;
	}

	public String getVirtuosoBaseGraph() {
		return virtuosoBaseGraph;
	}

	public String getMetaGraphName() {
		return virtuosoBaseGraph + "_meta";
	}

	public String getEntitySimilarityUri() {
		return virtuosoBaseGraph + "_entity_similarity";
	}

	public String getAttributeSimilarityUri() {
		return virtuosoBaseGraph + "_attribute_similarity";
	}

	public String getCustomEntityUri() {
		return getVirtuosoBaseGraph() + "/entity/";
	}

	public String getVirtuosoHostUrl() {
		return virtuosoHostUrl;
	}

	public LanguageCode getLanguageCode() {
		return parameters.languageCode;
	}

	public SpotlightConnector getSpotlightConnector() {
		return data.spotlightConnector;
	}

	public JSONProperties getJsonProperties() {
		return data.jsonProperties;
	}

	public String getDbpediaSparqlEndpoint() {
		return data.dbpediaSparqlEndpoint;
	}

	public Map<String, ColorEntity> getColors() {
		return data.colors;
	}

	public VirtuosoDataSource getVirtuosoDatasource() {
		return data.virtuosoDatasource;
	}

	public int getNumberOfDbpediaTriples() {
		return data.numberOfDbpediaTiples;
	}

	public String getPropertySizesFileName() {
		return data.propertySizesFileName;
	}

	public static Customer getCustomer() {
		return customer;
	}

	public String getSrcSchema() {
		return srcSchema;
	}

	public String getTargetSchema() {
		return targetSchema;
	}

	public String getVirtuosoSparqlEndpoint() {
		return virtuosoSparqlEndpoint;
	}

	public String getDbpediaNamespace() {
		return dbpediaNamespace;
	}
	
	private void initMainProperties(String props) {
		Properties properties = FileUtils.readProperties(props);
		data.recommenderProperties = new RecommenderProperties(properties);
	}
	
	private void init() {
		LanguageCode languageCode = parameters.languageCode;
		if (customer == null) {
			customer = Customer.melovely;
		}
		initDBProperties(getCustomerPropertiesPath() + "/database.properties");
		initJSONProperties(getCustomerPropertiesPath() + "/properties.json");
		initVirtuosoDatasource(propertiesDirPath + "main.properties");
		initSparqlProperties(propertiesDirPath + data.sparqlPropertiesPath);
		initLangProperties(propertiesDirPath + languageCode.name().toLowerCase());
		initMainProperties(propertiesDirPath + "main.properties");
		if (customer.equals(Customer.melovely) && parameters.dbInitMode) {
			try {
				initColors(propertiesDirPath, data.colorsPath);
			} catch (SQLGrammarException ex) {
				data.logger.warn("Colors were not initialised because: " + ex.getMessage());
			}
		}
	}

	private void initColors(String propertiesPath, String colorsPath) {
		try {
			File file = new File(propertiesPath + colorsPath);
			String fileText = FileUtils.readTextFromFile(file);
			for (String line : fileText.split("\n")) {
				String[] lineParts = line.split("\t");
				String enName = lineParts[0].trim();
				String gerName = lineParts[1].trim();
				String hex = lineParts[2].trim();
				String rgb = lineParts[3].trim();

				ColorEntity color = new ColorEntity(gerName, rgb);
				color.setHex(hex);
				color.addColorName(LanguageCode.EN, enName);
				String name = color.getName();
				data.colors.put(name, color);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initDBProperties(String dbProperties) {
		Properties databaseProperties = FileUtils.readProperties(dbProperties);
		srcSchema = databaseProperties.getProperty(HibernateConfigurator.DB_SRC_SCHEMA);
		targetSchema = databaseProperties.getProperty(HibernateConfigurator.DB_TARGET_SCHEMA);
	}

	protected void initJSONProperties(String propertiesPath) {

		String jsonText = FileUtils.readTextFromFile(propertiesPath);
		Gson gson = new Gson();

		data.jsonProperties = gson.fromJson(jsonText, JSONProperties.class);

	}

	private void initSparqlProperties(String sparqlProperties) {
		Properties properties = getXMLProperties(sparqlProperties);
		this.data.sparqlProperties = properties;

	}

	private Properties getXMLProperties(String propertiesPath) {
		Properties properties = new Properties();
		try {
			FileInputStream is = new FileInputStream(propertiesPath);

			properties.loadFromXML(is);
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	private void initLangProperties(String langProps) {
		Properties properties = FileUtils.readProperties(langProps + "/semrecsys.properties");
		data.spotlightUrl = properties.getProperty("SPOTLIGHT_URL");
		data.namespacer = initNamespacer(properties);
		data.textExtractor = initTextExtractor(properties);
		data.spotlightConnector = initSpotlightConnector(data.spotlightUrl);
		data.dbpediaSparqlEndpoint = properties.getProperty("DBPEDIA_ENDPOINT");
		data.numberOfDbpediaTiples = Integer.valueOf(properties.getProperty("NUMBER_OF_DBPEDIA_TRIPLES"));
		data.propertySizesFileName = langProps + "/" + "property_sizes.tsv";
	}

	private TextExtractor initTextExtractor(Properties properties) {
		TextExtractor productAnalyzer = null;

		try {
			productAnalyzer = new TextExtractor(parameters.languageCode);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return productAnalyzer;
	}

	private Namespacer initNamespacer(Properties properties) {
		Namespacer namespacer = new Namespacer();
		dbpediaNamespace = properties.getProperty("DBPEDIA_NAMESPACE");
		String dbpediaCategoryNamespace = properties.getProperty("DBPEDIA_CATEGORY_NAMESPACE");
		namespacer.add(properties.getProperty("DBPEDIA_FILE_NAMESPACE"), "file");
		namespacer.add(getPropertiesUri(), "prop");
		namespacer.add(getCustomEntityUri(), "meta");

		namespacer.add(dbpediaCategoryNamespace, "cat");
		namespacer.add(dbpediaNamespace, "");
		return namespacer;
	}

	private SpotlightConnector initSpotlightConnector(String spotlightUrl) {
		SpotlightConnector connector = new SpotlightConnector(spotlightUrl);
		return connector;
	}

	private void initVirtuosoDatasource(String props) {
		// "Example3",
		// "jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba",
		// "dba"
		Properties properties = FileUtils.readProperties(props);
		virtuosoHostUrl = properties.getProperty("VIRTUOSO_URI");
		virtuosoBaseGraph = virtuosoHostUrl + "/" + data.jsonProperties.getCustomer();
		virtuosoSparqlEndpoint = "http://" + properties.getProperty("VIRTUOSO_HOST")  + ":8890/" + "sparql";
		VirtuosoDataSource virtuosoDatasource = new VirtuosoDataSource();
		virtuosoDatasource.setServerName(properties.getProperty("VIRTUOSO_HOST"));
		virtuosoDatasource.setPortNumber(Integer.valueOf(properties.getProperty("VIRTUOSO_PORT")));
		virtuosoDatasource.setCharset(properties.getProperty("VIRTUOSO_CHARSET"));
		virtuosoDatasource.setUser(properties.getProperty("VIRTUOSO_USER"));
		virtuosoDatasource.setPassword(properties.getProperty("VIRTUOSO_PASSWORD"));

		this.data.virtuosoDatasource = virtuosoDatasource;
	}

	private String getPropertiesUri() {
		return getVirtuosoBaseGraph() + "/prop#";

	}

}
