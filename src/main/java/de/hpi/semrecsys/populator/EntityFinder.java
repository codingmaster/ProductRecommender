package de.hpi.semrecsys.populator;

import java.util.*;
import java.util.Map.Entry;

import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.utils.CollectionUtils;
import org.apache.log4j.Logger;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.Attribute.AttributeType;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.CustomEntity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.spotlight.SpotlightConnector;
import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;
import de.hpi.semrecsys.spotlight.SpotlightRequest;
import de.hpi.semrecsys.spotlight.SpotlightResponse;
import de.hpi.semrecsys.spotlight.SpotlightResponse.ResponseResource;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.utils.TextExtractor;

/**
 * finds entities from the given text using Spotlight Webservice
 * @see <a href="https://github.com/dbpedia-spotlight/dbpedia-spotlight/wiki">Spotlight Webservice</a>
 *
 */
public class EntityFinder {

	private static final double MINIMAL_SIM_THRESHOLD = 0.6;
	private static final double MINIMAL_ATTRIBUTE_CONFIDENCE = 0.404;
	private static final Double PLAIN_TEXT_CONFIDENCE = 0.404;
	private static final Double PLAIN_TEXT_SIM_THRESHOLD = 0.999;
    Integer max_number_of_entities = RecommenderProperties.MAX_NUMBER_OF_ENTITIES;
	private TextExtractor textExtractor;
	private SpotlightConnector spotlightConnector;
	private Namespacer namespacer;
	private double simThreshold;

	private EndpointType endpointType;
	Logger log = Logger.getLogger(getClass());
	private double confidence;
	private AttributeEntityMapping attributeEntitiesMapping;

	public EntityFinder(SemRecSysConfigurator configurator) {
		init(configurator);
	}

	private void init(SemRecSysConfigurator configurator) {
		textExtractor = configurator.getTextExtractor();
		spotlightConnector = configurator.getSpotlightConnector();
		namespacer = configurator.getNamespacer();
		endpointType = EndpointType.ANNOTATE;
		attributeEntitiesMapping = new AttributeEntityMapping(configurator);

	}

    /**
     * returns entities found in text with confidence
     * @param text
     * @param confidence
     * @return map from entity to list of response resources
     */
	public Map<Entity, List<ResponseResource>> getEntitiesFromText(String text, Double confidence) {
		List<ResponseResource> resources = getResourceEntities(text, confidence);
		return getEntityResponseResourceMap(resources);
	}

    /**
     * returns mapping from attribute to entities for the given product
     * @param product
     * @return attribute entity mapping
     */
	public AttributeEntityMapping getAttributeEntityMapping(Product product) {
		attributeEntitiesMapping.setProduct(product);
		// for each string attribute in product
		for (List<Attribute> attributeList : product.getAttributes().values()) {
			for (Attribute attribute : attributeList) {
				if (attribute.getType().equals(AttributeType.unstruct)) {
					processUnstructAttribute(attribute, attributeEntitiesMapping);
				} else {
					processStructAttribute(attribute, attributeEntitiesMapping);
				}
			}
		}
		return attributeEntitiesMapping;
	}

	private AttributeEntityMapping processStructAttribute(Attribute attribute,
			AttributeEntityMapping attributeEntityMapping) {
		Entity entity = new Entity(attribute.getValue(), "meta");
		entity.setMeta(true);
		AttributeEntity attributeEntity = new AttributeEntity(attribute, entity);
		attributeEntityMapping.addAttributeEntity(attributeEntity);
		return attributeEntityMapping;
	}

	private AttributeEntityMapping processUnstructAttribute(Attribute attribute,
			AttributeEntityMapping attributeEntityMapping) {
		List<ResponseResource> resources = getResources(attribute);
		if (!resources.isEmpty()) {
			Map<Entity, List<ResponseResource>> entityResponseResourceMap = getEntityResponseResourceMap(resources);
            entityResponseResourceMap = getMostFrequentEntities(entityResponseResourceMap);

			for (Entry<Entity, List<ResponseResource>> mapEntry : entityResponseResourceMap.entrySet()) {

				Entity entity = mapEntry.getKey();

                    List<ResponseResource> responseList = mapEntry.getValue();
                    AttributeEntity attributeEntity = new AttributeEntity(attribute, entity);
                    attributeEntity.addResponseResources(responseList);
                    attributeEntityMapping.addAttributeEntity(attributeEntity);
			}

		}
		return attributeEntityMapping;
	}

    private Map<Entity, List<ResponseResource>> getMostFrequentEntities(Map<Entity, List<ResponseResource>> entityResponseResourceMap) {
        Map<Entity, List<ResponseResource>> result = new HashMap<Entity, List<ResponseResource>>();
        Map<Entity, Integer> mostFrequentEntities = new TreeMap<Entity, Integer>();
        for(Entity key : entityResponseResourceMap.keySet()){
            Integer numberOfResources = entityResponseResourceMap.get(key).size();
            mostFrequentEntities.put(key, numberOfResources);
        }

        mostFrequentEntities = CollectionUtils.sortByValueDesc(mostFrequentEntities, max_number_of_entities);
        for(Entity key : mostFrequentEntities.keySet()){
            result.put(key, entityResponseResourceMap.get(key));
        }
        return result;
    }

    private List<ResponseResource> getResources(Attribute attribute) {
		updateConfidenceAndThreshold(attribute);
		String text = attribute.getValue();
		List<ResponseResource> resources = new ArrayList<SpotlightResponse.ResponseResource>();

		List<ResponseResource> resourceEntities = getResourceEntities(text, confidence);
		if (resourceEntities.isEmpty()) {
			resourceEntities = getResourceEntities(text, MINIMAL_ATTRIBUTE_CONFIDENCE);
		}
		resources.addAll(resourceEntities);

		return resources;
	}

	private void updateConfidenceAndThreshold(Attribute attribute) {
		confidence = MINIMAL_ATTRIBUTE_CONFIDENCE;
		simThreshold = MINIMAL_SIM_THRESHOLD;
		if (attribute.getType().equals(AttributeType.unstruct)) {
			confidence = PLAIN_TEXT_CONFIDENCE;
			simThreshold = PLAIN_TEXT_SIM_THRESHOLD;
		}
	}

	/**
	 * Executes request to Spotlight Endpoint and returns entity resources from
	 * it
	 * 
	 * @param text
	 *            input text
	 * @return list of response entity resources
	 */
	private List<ResponseResource> getResourceEntities(String text, Double confidence) {
		SpotlightRequest spotlightRequest = new SpotlightRequest(text, confidence);
		return getResourceEntities(spotlightRequest);

	}

	private List<ResponseResource> getResourceEntities(SpotlightRequest request) {
		SpotlightResponse spotlightResponse = spotlightConnector.getSpotlightResponse(request, endpointType);

		List<ResponseResource> resources = spotlightResponse.getResources();
		String text = request.getText();

		// get resources from splitted text
		text = textExtractor.getSplittedText(text).replace("\n", "");
		if (!text.isEmpty()) {
			request.setText(text);
			spotlightResponse = spotlightConnector.getSpotlightResponse(request, endpointType);
			resources = getResourcesWithSimThreshold(resources);
		}
		return resources;
	}

	private List<ResponseResource> getResourcesWithSimThreshold(List<ResponseResource> resources) {
		List<ResponseResource> result = new ArrayList<SpotlightResponse.ResponseResource>();

		for (ResponseResource resource : resources) {
			if (resource.getSimilarity() >= simThreshold) {
				result.add(resource);
			}
		}

		return result;
	}

	protected Map<Entity, List<ResponseResource>> getEntityResponseResourceMap(List<ResponseResource> resources) {
		Map<Entity, List<ResponseResource>> entityResponseResourceMap = new HashMap<Entity, List<ResponseResource>>();
		for (ResponseResource resource : resources) {
			String resourceUri = resource.getURI();

			String namespacedUri = namespacer.process(resourceUri);
			Entity entity = new Entity(namespacedUri);
			if (resource.isMeta()) {
				String uri = namespacedUri.replace("meta:", "");
				entity = new CustomEntity(uri);
			}

			List<ResponseResource> entityResources = entityResponseResourceMap.get(entity);
			if (entityResources == null) {
				entityResources = new ArrayList<ResponseResource>();
			}
			entityResources.add(resource);
			entityResponseResourceMap.put(entity, entityResources);

		}
		return entityResponseResourceMap;
	}

}
