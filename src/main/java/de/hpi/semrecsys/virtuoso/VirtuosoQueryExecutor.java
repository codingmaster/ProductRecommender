package de.hpi.semrecsys.virtuoso;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import virtuoso.jdbc3.VirtuosoDataSource;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResult;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix.EntityTuple;
import de.hpi.semrecsys.utils.DatatypeHelper;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager.QueryType;

/**
 * Executes virtuoso queries
 */
public class VirtuosoQueryExecutor {

	private VirtuosoDataSource dataSource;
	private final SemRecSysConfigurator configurator;
	private SparqlQueryManager queryManager;
	ProductDAO productManager = ProductDAO.getDefault();
	private Namespacer namespacer;
	protected final Log log = LogFactory.getLog(getClass());
	private VirtGraph graph;

	public VirtuosoQueryExecutor(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		init(configurator);
	}

	private void init(SemRecSysConfigurator configurator) {
		this.dataSource = configurator.getVirtuosoDatasource();
		this.queryManager = new SparqlQueryManager(configurator);
		this.namespacer = configurator.getNamespacer();
		this.graph = new VirtGraph(configurator.getVirtuosoBaseGraph(), dataSource);

	}


    /**
     * returns an object containing mapping between attributes and entities for the given product
     * @param product
     * @param queryType
     * @return
     */
	public AttributeEntityMapping getAttributeEntityMapping(Product product, QueryType queryType) {
		String queryString = queryManager.getQuery(queryType, product);
		ResultSet results = executeQuery(queryString);
		AttributeEntityMapping attributeEntityHolder = getAttributeEntityMapping(results, queryString);

		return attributeEntityHolder;
	}

    /**
     * add similar entities to the recommendation result
     * @param queryType
     * @param recommendationResult
     * @return recommendation result with new entities
     */
	public RecommendationResult addSimilarEntities(QueryType queryType, RecommendationResult recommendationResult) {
		String queryString = queryManager.getQuery(queryType, recommendationResult.getBaseProduct(),
				recommendationResult.recommendedProduct());
		ResultSet results = executeQuery(queryString);
		if (results != null) {
			while (results.hasNext()) {
				QuerySolution result = results.nextSolution();
				EntityTuple tuple = getEntityTuple(result);
				recommendationResult.addCommonEntityTuple(tuple);
			}
		}

		return recommendationResult;
	}

	public boolean isProductExists(Product product) {
		String queryString = queryManager.getQuery(QueryType.IS_PRODUCT_EXISTS, product);
		ResultSet resultSet = executeQuery(queryString);
		return resultSet.hasNext();
	}

	private ResultSet executeQuery(String queryString) throws IllegalStateException {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(queryString, graph);
		ResultSet results = null;
		try {
			results = vqe.execSelect();
			if (!results.hasNext()) {
				log.debug("Result set is empty\nquery: " + queryString);
			}
		} catch (JenaException ex) {
			throw new IllegalStateException("Query is illegal: " + queryString, ex);
		}

		return results;
	}

	private AttributeEntityMapping getAttributeEntityMapping(ResultSet results, String queryString) {
		AttributeEntityMapping attributeEntityMapping = new AttributeEntityMapping(configurator);

		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			AttributeEntity attributeEntity = getAttributeEntity(result, queryString);
			attributeEntityMapping.addAttributeEntity(attributeEntity);
		}
		return attributeEntityMapping;

	}

	private EntityTuple getEntityTuple(QuerySolution result) {
		Iterator<String> varNames = result.varNames();
		Entity entity1 = new Entity();
		Integer count1 = 0;
		Entity entity2 = new Entity();
		Integer count2 = 0;
		Double attributeWeight = null;
		Double similarityValue = null;
		String attributeType = null;
		while (varNames.hasNext()) {
			String varName = varNames.next();
			RDFNode node = result.get(varName);
			QueryVariable queryVariable = QueryVariable.valueOf(varName);

			switch (queryVariable) {
			case graph:
				break;
			case entity:
				String entityUri = node.asNode().getURI();
				entity1 = new Entity(namespacer.process(entityUri));
				break;
			case entity2:
				String entityUri2 = node.asNode().getURI();
				entity2 = new Entity(namespacer.process(entityUri2));
				break;
			case simvalue:
				Object literalValue = node.asNode().getLiteralValue();
				similarityValue = DatatypeHelper.getDoubleValue(literalValue);
				break;
			case simrank:
				break;
			case attribute_type:
				attributeType = namespacer.process(node.asNode().getURI());
				break;
			case attribute_weight:
				Object value = node.asNode().getLiteralValue();
				attributeWeight = DatatypeHelper.getDoubleValue(value);
				break;
			case count:
				count1 = (Integer) node.asNode().getLiteralValue();
				break;
			case count2:
				count2 = (Integer) node.asNode().getLiteralValue();
				break;
			default:
				throw new IllegalStateException("QueryVariable " + queryVariable + " shouldnt exist in result set");

			}

		}

		entity1.setCount(count1);

		entity2.setCount(count2);

		EntityTuple entityTuple = new EntityTuple(entity1, entity2);
		// entityTuple.setSimilarityRank(rank);
		entityTuple.setSimilarityValue(similarityValue);
		entityTuple.setAttributeType(attributeType.replace("prop:", ""));
		entityTuple.setAttributeWeight(attributeWeight);
		return entityTuple;
	}

	private AttributeEntity getAttributeEntity(QuerySolution result, String queryString) {
		Iterator<String> varNames = result.varNames();

		Integer count = null;
		Entity entity = null;
		Attribute attribute = null;
		boolean isMeta = false;
		while (varNames.hasNext()) {
			String varName = varNames.next();
			RDFNode node = result.get(varName);
			QueryVariable queryVariable = QueryVariable.valueOf(varName);
			switch (queryVariable) {
			case graph:
				break;
			case attribute_type:
				String attributeType = node.asNode().getURI().split("#")[1];
				attribute = new Attribute(attributeType, "dummy value", -1L);
				break;
			case uri:
				String entityUri = node.asNode().getURI();
				entity = new Entity(namespacer.process(entityUri));
				break;
			case count:
				count = (Integer) node.asNode().getLiteralValue();
				break;
			case ismeta:
				isMeta = (Integer) node.asNode().getLiteralValue() == 1;
				break;
			default:
				throw new IllegalStateException("QueryVariable " + queryVariable + " shouldnt exist in result set "
						+ "\nQuery:\n" + queryString);
			}
		}
		entity.setMeta(isMeta);
		AttributeEntity attributeEntity = new AttributeEntity(attribute, entity);
		attributeEntity.setCount(count);

		log.debug(attributeEntity);
		return attributeEntity;
	}

	public enum QueryVariable {
		graph, product, product_name, weight, uri, attribute_type, count, depth, entity, entity2, simvalue, simrank, ismeta, attribute_weight, count2;

		@Override
		public String toString() {
			return "?" + this.name();
		}
	}

}
