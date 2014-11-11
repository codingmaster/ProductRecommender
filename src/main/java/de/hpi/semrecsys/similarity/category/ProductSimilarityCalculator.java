package de.hpi.semrecsys.similarity.category;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResult;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.utils.CollectionUtils;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager.QueryType;
import de.hpi.semrecsys.virtuoso.VirtuosoQueryExecutor;

/**
 * Calculates similarity of two products
 * 
 * @author michael
 * 
 */
public class ProductSimilarityCalculator {

	Logger log = Logger.getLogger(getClass());
	private SemRecSysConfigurator configurator;
	private VirtuosoQueryExecutor queryExecutor;

	public ProductSimilarityCalculator(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.queryExecutor = new VirtuosoQueryExecutor(configurator);
	}

	public void setLogLevel(Level level) {
		log.setLevel(level);
	}

	public void fillProductWithAttributeEntityMapping(Product product) {
		AttributeEntityMapping attributeEntityMapping = product.getAttributeEntityMapping();
		if (attributeEntityMapping == null) {
			attributeEntityMapping = queryExecutor.getAttributeEntityMapping(product, QueryType.ATTRIBUTE_ENTITY_LIST);
			product.setAttributeEntityMapping(attributeEntityMapping);
		}
	}

	public RecommendationResult fillWithSimilarEntities(QueryType queryType, RecommendationResult recommendationResult) {
		if(recommendationResult.getCommonEntities() == null || recommendationResult.getCommonEntities().isEmpty()) {
            recommendationResult = queryExecutor.addSimilarEntities(queryType, recommendationResult);
            recommendationResult.calculateProductSimilarity(configurator.getJsonProperties().getAttributesByType());
        }
		return recommendationResult;
	}

	/**
	 * Returns map of entity and its value. Value is calculated as entityValue1
	 * * entityValue2
	 * 
	 * @param product1
	 * @param product2
	 * @return
	 */
	public Map<Entity, Double> getCommonEntities(Product product1, Product product2) {

		fillProductWithAttributeEntityMapping(product1);
		fillProductWithAttributeEntityMapping(product2);

		AttributeEntityMapping attributeEntityHolder1 = product1.getAttributeEntityMapping();
		AttributeEntityMapping attributeEntityHolder2 = product2.getAttributeEntityMapping();

		Map<Entity, Double> entityValuesMap1 = attributeEntityHolder1.calculateEntityValueMap();
		Map<Entity, Double> entityValuesMap2 = attributeEntityHolder2.calculateEntityValueMap();
		Map<Entity, Double> commonEntities = getCommonEntities(entityValuesMap1, entityValuesMap2);
		return commonEntities;

	}


	public Map<Entity, Double> getCommonEntities(Map<Entity, Double> entityValuesMap1,
			Map<Entity, Double> entityValuesMap2) {
		Map<Entity, Double> commonEntities = new HashMap<Entity, Double>();
		for (Entry<Entity, Double> entry1 : entityValuesMap1.entrySet()) {
			for (Entry<Entity, Double> entry2 : entityValuesMap2.entrySet()) {
				if (entry1.getKey().equals(entry2.getKey())) {
					Double value = entry1.getValue() * entry2.getValue();
					commonEntities.put(entry1.getKey(), value);
					break;
				}
			}
		}
		return CollectionUtils.sortByValueDesc(commonEntities);
	}

}
