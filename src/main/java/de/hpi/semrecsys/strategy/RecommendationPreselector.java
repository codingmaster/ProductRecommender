package de.hpi.semrecsys.strategy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hpi.semrecsys.persistence.ProductDAO;
import org.apache.log4j.Logger;

import virtuoso.jdbc3.VirtuosoDataSource;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResultsHolder;
import de.hpi.semrecsys.similarity.category.ProductSimilarityCalculator;
import de.hpi.semrecsys.utils.DatatypeHelper;
import de.hpi.semrecsys.virtuoso.ProductTriplesCreator;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager.QueryType;
import de.hpi.semrecsys.virtuoso.VirtuosoQueryExecutor.QueryVariable;

/**
 * First step in selection similar attributes
 * 
 * @author michael
 * 
 */
public class RecommendationPreselector {

	private final SemRecSysConfigurator configurator;
	ProductDAO productManager = ProductDAO.getDefault();
	ProductSimilarityCalculator similarityCalculator;
	Logger log = Logger.getLogger(getClass());

	public RecommendationPreselector(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.similarityCalculator = new ProductSimilarityCalculator(configurator);
		configurator.getRecommenderProperties();
	}

	public RecommendationResultsHolder getPreselectedSimilarProducts(Product product1, QueryType queryType, int number) {
		RecommendationResultsHolder recommendationResults = new RecommendationResultsHolder(product1);
		SparqlQueryManager queryManager = new SparqlQueryManager(configurator);
		VirtuosoDataSource dataSource = configurator.getVirtuosoDatasource();

		queryManager.setLimit(number);
		String queryString = queryManager.getQuery(queryType, product1);
		VirtGraph graph = new VirtGraph(configurator.getVirtuosoBaseGraph(), dataSource);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(queryString, graph);
		ResultSet result = null;
		try {
			result = vqe.execSelect();
		} catch (Exception ex) {
			log.error("Query execution failed. Query: \n " + queryString, ex);
		}
		if (!result.hasNext()) {
			log.error("Result set is empty. Query: " + queryString);
		}
		Map<Product, Double> productCountMap = processQueryResult(result);
		recommendationResults.addAll(productCountMap);
		return recommendationResults;
	}


    public RecommendationResultsHolder calculateProductSimilarity(Product product1, Product product2){
        RecommendationResultsHolder recommendationResults = new RecommendationResultsHolder(product1);
        SparqlQueryManager queryManager = new SparqlQueryManager(configurator);
        VirtuosoDataSource dataSource = configurator.getVirtuosoDatasource();

        String queryString = queryManager.getQuery(QueryType.PRODUCT_SIMILARITY, product1, product2);
        VirtGraph graph = new VirtGraph(configurator.getVirtuosoBaseGraph(), dataSource);
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(queryString, graph);
        ResultSet result = null;
        try {
            result = vqe.execSelect();
        } catch (Exception ex) {
            log.error("Query execution failed. Query: \n " + queryString, ex);
        }
        if (!result.hasNext()) {
            log.error("Result set is empty. Query: " + queryString);
        }
        Map<Product, Double> productCountMap = processQueryResult(result);
        recommendationResults.addAll(productCountMap);
        return recommendationResults;

    }



    private Map<Product, Double> processQueryResult(ResultSet results) {
		Map<Product, Double> productCountMap = new HashMap<Product, Double>();
		int counter = 0;
		while (results.hasNext()) {
			Double count = null;
			Product product = null;
			QuerySolution result = results.nextSolution();
			Iterator<String> varNames = result.varNames();
			while (varNames.hasNext()) {
				String varName = varNames.next();
				RDFNode node = result.get(varName);
				QueryVariable queryVariable = QueryVariable.valueOf(varName);

				switch (queryVariable) {
				case product:
					String productUri = node.asNode().getURI();
					Integer productId = ProductTriplesCreator.getProductIdFromUri(productUri);
					product = productManager.findById(productId);
					if (product == null || product.getTitle() == null) {
						continue;
					}
					break;
				case count:
					Object literalValue = node.asNode().getLiteralValue();
					count = DatatypeHelper.getDoubleValue(literalValue);
					break;
				default:
					break;
				}
			}
			if (counter > RecommenderProperties.NUMBER_OF_RESULTS) {
				break;
			}
			productCountMap.put(product, count);
			counter++;
		}
		return productCountMap;
	}

}
