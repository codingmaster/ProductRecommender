package de.hpi.semrecsys.populator;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.graph.GraphOps;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute.AttributeType;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.virtuoso.AbstractTriplesCreator;
import de.hpi.semrecsys.virtuoso.AttributeSimilarityTriplesCreator;
import de.hpi.semrecsys.virtuoso.EntitySimilarityTriplesCreator;
import de.hpi.semrecsys.virtuoso.MetaTriplesCreator;
import de.hpi.semrecsys.virtuoso.ProductTriplesCreator;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager.QueryType;
import de.hpi.semrecsys.virtuoso.VirtuosoQueryExecutor;
import de.hpi.semrecsys.webservice.SparqlEndpointConnector;

/**
 * Fills Virtuoso with triples created by triples creator
 * 
 * @author Michael Wolowyk
 * 
 */
public class Populator {

	protected final Log log = LogFactory.getLog(getClass());
	/**
	 * output after PRINT_AFTER lines
	 */
	public static int PRINT_AFTER = 10;

	private final SemRecSysConfigurator configurator;
	private final ProductDAO productManager = ProductDAO.getDefault();
	private VirtuosoQueryExecutor queryExecutor;
	
	/**
	 * Options show, which graphs to populate
	 */
	public static enum PopulationOption {
		/**
		 * all graphs
		 */
		all, 
		/**
		 * Meta information containing attribute type weights
		 */
		meta, 
		/**
		 * Information about products
		 */
		products, 
		/**
		 * Graph containing similarity between entities
		 */
		entity_sim, 
		/**
		 * Graph containing similarity between attributes
		 */
		attribute_sim, 
		/**
		 * Small product data set for test cases
		 */
		products_small
	}

	public Populator(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.queryExecutor = new VirtuosoQueryExecutor(configurator);
	}

	/**
	 * Populates Virtuoso Service
	 * @param numberOfProducts number of products, which are used for the population
	 * @param clean delete graph before population
	 * @param options population options
	 */
	public void populate(int numberOfProducts, boolean clean, PopulationOption... options) {
		List<PopulationOption> optionList = Arrays.asList(options);
		if (optionList.contains(PopulationOption.meta) || optionList.contains(PopulationOption.all)) {
			populateMeta(clean, configurator.getMetaGraphName());
		}
		if (optionList.contains(PopulationOption.attribute_sim) || optionList.contains(PopulationOption.all)) {
			populateAttributeSimilarity(clean, configurator.getAttributeSimilarityUri());
		}
		if (optionList.contains(PopulationOption.products) || optionList.contains(PopulationOption.all)) {
			populateProducts(clean, configurator.getVirtuosoBaseGraph());
		}
		if (optionList.contains(PopulationOption.products_small)) {
			populateProducts(clean, configurator.getVirtuosoBaseGraph(), numberOfProducts);
		}
		if (optionList.contains(PopulationOption.entity_sim) || optionList.contains(PopulationOption.all)) {
			populateEntitySimilarity(-1, clean, configurator.getEntitySimilarityUri());
		}
		String resultString = getGraphSizes();
		System.out.println(resultString);
	}
	
	/**
	 * Populate Virtuoso Service
	 * @param clean delete graph before population
	 * @param options population options
	 */
	public void populate(boolean clean, PopulationOption... options){
		 populate(-1, clean, options);
	}
	
	
	/**
	 * Populate Meta graph
	 * @param clean delete graph before population
	 * @param graphName 
	 * @param attributeTypes restrict population to some attribute types
	 * @return number of triples added to the graph
	 */
	public int populateMeta(boolean clean, String graphName, AttributeType... attributeTypes) {
		MetaTriplesCreator nodeCreator = new MetaTriplesCreator(configurator);
		nodeCreator.setAttributeTypes(attributeTypes);
		return populate(clean, graphName, nodeCreator, -1);
	}

	/**
	 * Populate Meta graph
	 * @param cleanGraph delete graph before population
	 * @param graphName 
	 * @return number of triples added to the graph
	 */
	public int populateMeta(boolean cleanGraph, String graphName) {
		AbstractTriplesCreator nodeCreator = new MetaTriplesCreator(configurator);
		return populate(cleanGraph, graphName, nodeCreator, -1);
	}

	/**
	 * Delete graph
	 * @param graphName
	 */
	public void cleanGraph(String graphName) {
		VirtGraph graph = new VirtGraph(graphName, configurator.getVirtuosoDatasource());
		cleanGraph(true, graph);
	}
	
	/**
	 * Get size of the graph
	 * @param graphName
	 * @return size of the graph
	 */
	public int getGraphSize(String graphName) {
		VirtGraph graph = new VirtGraph(graphName, configurator.getVirtuosoDatasource());
		return graph.size();
	}

    /**
     * Get available Virtuoso graphs
     * @return
     */
	public String getGraphSizes() {
		SparqlQueryManager queryManager = new SparqlQueryManager(configurator);
		String statQuery = queryManager.getQuery(QueryType.GRAPH_SIZES);
		ResultSet resultSet = SparqlEndpointConnector.executeQuery(statQuery, configurator.getVirtuosoSparqlEndpoint());
		String resultString = ResultSetFormatter.asText(resultSet);
		return resultString;
	}

	
	private int populateEntitySimilarity(int number, boolean clean, String graphName) {
		AbstractTriplesCreator triplesCreator = new EntitySimilarityTriplesCreator(configurator);
		populate(clean, graphName, triplesCreator, number);
		return getGraphSize(graphName);

	}

	private int populateProducts(boolean clean, String graphName) {
		int startId = productManager.getMinId();
		return populateProducts(clean, graphName, startId, -1);
	}

	private int populateProducts(boolean clean, String graphName, int number) {
		int startId = productManager.getMinId();
		return populateProducts(clean, graphName, startId, number);
	}

	private int populateProducts(boolean clean, String graphName, int startId, int number) {
		VirtGraph graph = new VirtGraph(graphName, configurator.getVirtuosoDatasource());
		log.info(productManager.getProductSize() + " products were found in the database");
		cleanGraph(clean, graph);
		populateProducts(startId, number, graph, clean);

		return graph.size();
	}

	private void populateProducts(int startId, int number, VirtGraph graph, boolean clean) {
		ProductTriplesCreator nodeCreator = new ProductTriplesCreator(configurator);
		long start = System.currentTimeMillis();
		int maxId = productManager.getMaxId();
		if (number < 1) {
			number = maxId - startId;
		}
		for (Integer i = startId; i < number + startId; i++) {
			if (i > maxId) {
				break;
			}
			Product product = productManager.findById(i);
			if (shouldBeExecuted(clean, product)) {
				start = printExecutionTime(start, i, product);

				try {
					List<Triple> allTriples = nodeCreator.createTriplesForProduct(product);
					GraphOps.addAll(graph, allTriples);
				} catch (Exception ex) {
					log.error("Exception for product " + product + " : " + ex.getMessage());
				}
			} else {
				log.warn("Skip product " + product);
			}
		}
	}

	private boolean shouldBeExecuted(boolean clean, Product product) {
		return product != null && product.getTitle() != null && !queryExecutor.isProductExists(product);
	}

	private long printExecutionTime(long start, Integer i, Product product) {
		if (i % PRINT_AFTER == 0) {
			log.info("Processing: " + i + " product " + product.getTitle());
			long end = System.currentTimeMillis();
			log.info("Time: " + String.valueOf(end - start) + " ms");

			start = System.currentTimeMillis();
		}
		return start;
	}

	private int populateAttributeSimilarity(boolean cleanGraph, String graphName) {
		AbstractTriplesCreator nodeCreator = new AttributeSimilarityTriplesCreator(configurator);
		return populate(cleanGraph, graphName, nodeCreator, -1);
	}

	private int populate(boolean cleanGraph, String graphName, AbstractTriplesCreator triplesCreator, int number) {
		log.info("Processing graph " + graphName + " ...");
		List<Triple> triples = triplesCreator.createTriples(number);
		VirtGraph graph = new VirtGraph(graphName, configurator.getVirtuosoDatasource());
		cleanGraph(cleanGraph, graph);
		log.info("Filling graph " + graphName + " with " + triples.size() + " triples...");
		GraphOps.addAll(graph, triples);
		int graphSize = getGraphSize(graphName);
		log.info("Size of graph " + graphName + " is " + graphSize);
		return graphSize;
	}



	private void cleanGraph(boolean clean, VirtGraph graph) {
		int size = graph.size();
		if (size > 0) {
			log.warn(graph.getGraphName() + " is not empty. Size: " + size);
			if (clean) {
				log.warn("Cleaning " + graph.getGraphName() + ". Size: " + graph.size());
				graph.clear();
			}
		}
	}



}
