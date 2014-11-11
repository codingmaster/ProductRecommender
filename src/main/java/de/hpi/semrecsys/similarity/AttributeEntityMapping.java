package de.hpi.semrecsys.similarity;

import java.util.*;

import de.hpi.semrecsys.config.RecommenderProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.utils.CollectionUtils;

/**
 * holds attributeEntitys. has inner class AttributeEntity<Attribute, Entity,
 * Integer>
 * 
 * @author michael
 * 
 */
public class AttributeEntityMapping {
	List<AttributeEntity> attributeEntities = new ArrayList<AttributeEntity>();
	Map<Attribute, Double> attributeCountMap = new HashMap<Attribute, Double>();
	Map<String, Double> attributeCoefficientsMap;
	Product product;
    int maxNumberOfEntities = RecommenderProperties.MAX_NUMBER_OF_ENTITIES;

	protected final Log log = LogFactory.getLog(getClass());
	private HashMap<String, Map<Entity, AttributeEntity>> groupedAttributeMap;

	public AttributeEntityMapping(SemRecSysConfigurator configurator) {
		this.attributeCoefficientsMap = configurator.getJsonProperties().getAttributesByType();
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public void addAttributeEntity(AttributeEntity attributeEntity) {
		attributeEntities.add(attributeEntity);
		addToAttributeCount(attributeEntity);
	}


	/**
	 * Calculates attributeCountMap. Sum of entities pro attribute
	 *
	 */
	private void addToAttributeCount(AttributeEntity attributeEntity) {
		Attribute attribute = attributeEntity.getAttribute();
		Integer weight = attributeEntity.getWeight();
		Double value = (Double) CollectionUtils.getValue(attributeCountMap, attribute, Double.class);
		value += weight;
		attributeCountMap.put(attribute, value);
	}

	public Map<Attribute, Double> getAttributeCountMap() {
		return attributeCountMap;
	}

	public List<AttributeEntity> getAttributeEntities() {
		return attributeEntities;
	}

	public Map<String, Map<Entity, AttributeEntity>> getGroupedAttributeEntities() {
		if (groupedAttributeMap == null) {
			groupedAttributeMap = new HashMap<String, Map<Entity, AttributeEntity>>();

			for (AttributeEntity aew : getAttributeEntities()) {
				String attribute = aew.getAttribute().getAttributeCode();
				Map<Entity, AttributeEntity> aewList = groupedAttributeMap.get(attribute);
				if (aewList == null) {
					aewList = new HashMap<Entity, AttributeEntity>();
				}
				aewList.put(aew.getEntity(), aew);
				groupedAttributeMap.put(attribute, aewList);
			}

		}
		return groupedAttributeMap;
	}

	/**
	 * Calculates entity value map {(E,v) | v = sum((depthcoeff * attributecoeff
	 * * value)/attributeCount)
	 * 
	 * @return
	 */
	public Map<Entity, Double> calculateEntityValueMap() {

		attributeCoefficientsMap = recalculateAttributeCoefficientsMap();

		Map<Entity, Double> result = new HashMap<Entity, Double>();
		for (AttributeEntity attributeEntity : getAttributeEntities()) {
			Attribute attribute = attributeEntity.getAttribute();
			Double attributeCount = attributeCountMap.get(attribute);
			Double attributeCoefficient = attributeCoefficientsMap.get(attribute);
			Integer weight = attributeEntity.getWeight();

			Double entityValue = (weight * attributeCoefficient) / attributeCount;
			Entity entity = attributeEntity.getEntity();
			log.debug(entity + "\t" + attribute + "\t" + entityValue);
			Double value = (Double) CollectionUtils.getValue(result, entity, Double.class);
			value += entityValue;
			result.put(entity, value);
		}
		return CollectionUtils.sortByValueDesc(result);
	}

	private Map<String, Double> recalculateAttributeCoefficientsMap() {
		Double coefficientSum = calculateCoefficientSum();
		if (coefficientSum < 1) {
			// distribute coefficient rest between existing attributes
			// proportionally
			for (String prodAttribute : attributeCoefficientsMap.keySet()) {
				Double coefficient = 0.0;
				if (attributeCountMap.containsKey(prodAttribute)) {
					coefficient = attributeCoefficientsMap.get(prodAttribute);
					coefficient = coefficient / coefficientSum;
				}

				attributeCoefficientsMap.put(prodAttribute, coefficient);
			}
		}
		if ((coefficientSum = calculateCoefficientSum()) != 1.0) {
			log.warn("Coefficients were distributed unequally. Rest: " + coefficientSum + "\n coefficientsMap: "
					+ attributeCoefficientsMap);
		}
		return attributeCoefficientsMap;
	}

	private Double calculateCoefficientSum() {
		Double coefficientSumRest = 0.0;

		for (Attribute prodAttribute : attributeCountMap.keySet()) {
			Double coefficient = attributeCoefficientsMap.get(prodAttribute);
			if (coefficient != null) {
				coefficientSumRest += coefficient;
			}
		}
		return coefficientSumRest;
	}

	@Override
	public String toString() {
		return getAttributeEntities().toString();
	}

}
