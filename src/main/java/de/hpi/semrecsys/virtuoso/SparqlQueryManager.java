package de.hpi.semrecsys.virtuoso;

import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Product;

/**
 * creates SPARQL queries
 */
public class SparqlQueryManager {

	private final ProductTriplesCreator resourceCreator;
	private SemRecSysConfigurator configurator;
	private static String customer;
	int limit = -1;
	private static double maxEntityThreshold;
	private static double minEntityThreshold;

	public SparqlQueryManager(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		customer = configurator.getJsonProperties().getCustomer();
		resourceCreator = new ProductTriplesCreator(configurator);
		configurator.getVirtuosoHostUrl();
		configurator.getRecommenderProperties();
		maxEntityThreshold = RecommenderProperties.MAX_ENTITY_SIMILARITY;
		configurator.getRecommenderProperties();
		minEntityThreshold = RecommenderProperties.MIN_ENTITY_SIMILARITY;
	}

	public String getQuery(QueryType queryType, Product... products) {
		String query = getQuery(queryType);
		int i = 0;
		for (Product product : products) {
			i++;
			query = setProduct(query, product, QueryPlaceholder.PRODUCT.name() + i);
		}
		query = replacePlaceholder(query, QueryPlaceholder.MAX_ENTITY_THRESHOLD.name(),
				String.valueOf(maxEntityThreshold));
		query = replacePlaceholder(query, QueryPlaceholder.MIN_ENTITY_THRESHOLD.name(),
				String.valueOf(minEntityThreshold));
		query = replacePlaceholder(query, QueryPlaceholder.DBPEDIA_PREFIX.name(),
				'"' + configurator.getDbpediaNamespace() + '"');
		return query;
	}

	private String getQuery(QueryType queryType) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(getBase() + "\n");
		queryBuilder.append(getPrefixes(queryType) + "\n");
		String query = configurator.getSparqlProperties().getProperty(queryType.name());
		query = setFromClause(queryType, query);
		query = setLimit(query, limit);
		queryBuilder.append(query);
		return queryBuilder.toString();
	}

	private String getPrefixes(QueryType queryType) {
		String prefix = "PREFIX prop: <" + customer + "/prop#> \n";
		return prefix;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private String getBase() {
		return "BASE <" + configurator.getVirtuosoHostUrl() + ">";
	}

	private String setFromClause(QueryType queryType, String query) {
		String[] sourceUris = { "<" + configurator.getMetaGraphName() + ">",
				"<" + configurator.getEntitySimilarityUri() + ">", "<" + configurator.getVirtuosoBaseGraph() + ">",
				"<" + configurator.getAttributeSimilarityUri() + ">" };
		String fromClause = "";
		for (String uri : sourceUris) {
			fromClause += "from " + uri + "\n";
		}

		return replacePlaceholder(query, QueryPlaceholder.FROM_CLAUSE.name(), fromClause);
	}

	private String replacePlaceholder(String query, String placeHolder, String value) {
		return query.replace("$" + placeHolder + "$", value);
	}

	private String setProduct(String query, Product product, String placeHolder) {
		String productUri = resourceCreator.createProductNode(product).getURI();
		return setBaseProduct(query, productUri, placeHolder);
	}

	private String setLimit(String query, int limit) {
		if (limit > 0) {
			String limitQuery = " LIMIT " + String.valueOf(limit);
			query += limitQuery;
		}
		return query;
	}

	private String setBaseProduct(String query, String uri, String placeHolder) {
		String value = "<" + uri + ">";
		return replacePlaceholder(query, placeHolder, value);
	}

	public enum QueryType {
		ATTRIBUTE_ENTITY_LIST, GET_ALL_ENTITIES, GET_ALL_DBPEDIA_ENTITIES, PRODUCTS_WITH_SIMILAR_ENTITIES, SIMILAR_ENTITIES_BETWEEN_PRODUCTS, IS_PRODUCT_EXISTS, PRODUCT_SIMILARITY, GRAPH_SIZES;
	}

	public enum QueryPlaceholder {
		PRODUCT, FROM_CLAUSE, SELECT_CLAUSE, MAX_ENTITY_THRESHOLD, MIN_ENTITY_THRESHOLD, DBPEDIA_PREFIX;
	}

}
