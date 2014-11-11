package de.hpi.semrecsys.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.hpi.semrecsys.config.RecommenderProperties;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.simentity.CategoryEntitySimilarityCalculator;
import de.hpi.semrecsys.simentity.EntitySimilarityCalculator;
import de.hpi.semrecsys.simentity.WikipageLinksEntitySimilarityCalculator;
import de.hpi.semrecsys.utils.CollectionUtils;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.utils.StringUtils;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager;
import de.hpi.semrecsys.virtuoso.SparqlQueryManager.QueryType;
import de.hpi.semrecsys.webservice.SparqlEndpointConnector;

/**
 * Matrix Containing Entity pairs and their similarity value Avoid reflexivity
 * and symmetry
 * 
 * @author michael
 * 
 */
public class EntitySimilarityMatrix {

	Map<Entity, Map<Entity, Double>> calculatedEntitiesMatrix;
	EntitySimilarityCalculator calculator;
	private double minSimThreshold;
	private Double simAddition;
	public static int NUMBER_OF_SIMILAR_ENTITIES = -1;
	private static final int CATEGORY_WEIGHT = 2;

	Logger log = Logger.getLogger(getClass());
	private SemRecSysConfigurator configurator;
	private Namespacer namespacer;
    private CategoryEntitySimilarityCalculator categoryEntitySimilarityCalculator;
    private WikipageLinksEntitySimilarityCalculator wikipageLinksEntitySimilarityCalculator;

    public EntitySimilarityMatrix(SemRecSysConfigurator configurator, int limit) {
		this.configurator = configurator;
		this.namespacer = configurator.getNamespacer();
		calculatedEntitiesMatrix = new HashMap<Entity, Map<Entity, Double>>();
		this.minSimThreshold = RecommenderProperties.MIN_ENTITY_SIMILARITY;
		this.simAddition = 0.0;
        categoryEntitySimilarityCalculator = CategoryEntitySimilarityCalculator.getDefault(configurator);
        wikipageLinksEntitySimilarityCalculator = WikipageLinksEntitySimilarityCalculator.getDefault(configurator);
		init(limit);
	}





	private void init(int limit) {
		Set<Entity> entities = getDBpediaEntitiesFromTripleStore(limit);
        log.info("Number of entities in Triple store: " + entities.size());
		Double similarity = 0.0;
		int entityCount = 0;
		for (Entity entity : entities) {
		    log.info(entityCount + " > " + entity);
			entityCount++;
            int entityCount2 = 0;
            int entitySize = entities.size();
			for (Entity entity2 : entities) {
				// System.out.println("\t" + entity2);
                if(entityCount < 2 && entityCount2 % 10 == 0){
                    log.info(" processing " + entityCount2 + " from " + entitySize) ;
                }
				if (!isSimilarityAlreadyCalculated(entity, entity2)) {
					similarity = calculateEntitySimilarity(entity, entity2);

					log.debug("Similarity " + entity + " : " + entity2 + " = " + similarity);
					if (similarity >= minSimThreshold) {
						putToCalculatedEntitiesMatrix(entity, entity2, similarity);
					}
				}
                entityCount2++;
			}
			putToCalculatedEntitiesMatrix(entity, entity, 1.0);

		}
	}

	private Double calculateEntitySimilarity(Entity entityA, Entity entityB) {
		Double result = 0.0;
		Double overallResult = 0.0;

		result = categoryEntitySimilarityCalculator.calculateSimilarity(entityA, entityB);
		overallResult += result * categoryEntitySimilarityCalculator.getApproachCoeff();

		result = wikipageLinksEntitySimilarityCalculator.calculateSimilarity(entityA, entityB);
		overallResult += result * wikipageLinksEntitySimilarityCalculator.getApproachCoeff();
        log.debug("Similarity " + entityA + ", " + entityB + " = " + overallResult);
		return overallResult;
	}

	public boolean isSimilarityAlreadyCalculated(Entity entity, Entity entity2) {
		EntityTuple entityTuple = new EntityTuple(entity, entity2);
		entity = entityTuple.first;
		entity2 = entityTuple.second;

		if (!entity.equals(entity2)
				&& (calculatedEntitiesMatrix.get(entity) == null || !calculatedEntitiesMatrix.get(entity).containsKey(
						entity2))) {
			return false;
		}
		return true;
	}

	public void putToCalculatedEntitiesMatrix(Entity entity, Entity entity2, Double similarity) {
		EntityTuple entityTuple = new EntityTuple(entity, entity2);
		if (similarity > 0.0) {
			Map<Entity, Double> calculatedEntities = calculatedEntitiesMatrix.get(entityTuple.first);
			if (calculatedEntities == null) {
				calculatedEntities = new HashMap<Entity, Double>();
			}
			calculatedEntities.put(entityTuple.second, similarity);
			calculatedEntitiesMatrix.put(entityTuple.first, calculatedEntities);
		}
	}

	public Double getEntitySimilarity(Entity entity, Entity entity2) {
		EntityTuple entityTuple = new EntityTuple(entity, entity2);
		Double similarity = null;
		Map<Entity, Double> similarityMap = calculatedEntitiesMatrix.get(entityTuple.first);
		if (similarityMap != null) {
			similarity = similarityMap.get(entityTuple.second);
		}
		return similarity;
	}

	public Map<Entity, Map<Entity, Double>> getCalculatedEntitiesMatrix() {
		for (Entry<Entity, Map<Entity, Double>> entry : calculatedEntitiesMatrix.entrySet()) {
			Entity entity = entry.getKey();
			Map<Entity, Double> similarEntitiesMap = entry.getValue();
			similarEntitiesMap = CollectionUtils.sortByValueDesc(similarEntitiesMap, NUMBER_OF_SIMILAR_ENTITIES);

			Map<Entity, Double> newMap = new HashMap<Entity, Double>();
			for (Entry<Entity, Double> similarityEntry : similarEntitiesMap.entrySet()) {
				Double similarity = similarityEntry.getValue();
				if (similarity < 1.0) {
					similarity += simAddition;
				}
				newMap.put(similarityEntry.getKey(), similarity);
			}
			calculatedEntitiesMatrix.put(entity, CollectionUtils.sortByValueDesc(newMap));

		}

		return calculatedEntitiesMatrix;
	}

	public Set<Entity> getDBpediaEntitiesFromTripleStore(int limit) {

		String sparqlQueryString1 = getAllDBpediaEntitiesQuery(limit);
		String var = "uri";
		Set<Entity> entities = getEntityList(sparqlQueryString1, configurator.getVirtuosoSparqlEndpoint(), var);
		return entities;
	}

	protected String getAllEntitiesQuery(int limit) {
		SparqlQueryManager queryManager = new SparqlQueryManager(configurator);
		queryManager.setLimit(limit);
		return queryManager.getQuery(QueryType.GET_ALL_ENTITIES);
	}

	protected String getAllDBpediaEntitiesQuery(int limit) {
		SparqlQueryManager queryManager = new SparqlQueryManager(configurator);
		queryManager.setLimit(limit);
		return queryManager.getQuery(QueryType.GET_ALL_DBPEDIA_ENTITIES);
	}

	private Set<Entity> getEntityList(String sparqlQueryString1, String endpoint, String... vars) {
		Set<Entity> entities = new HashSet<Entity>();
		ResultSet resultSet = SparqlEndpointConnector.executeQuery(sparqlQueryString1, endpoint);
		if (resultSet != null) {
			List<QuerySolution> results = ResultSetFormatter.toList(resultSet);
			for (QuerySolution querySolution : results) {

				String resultUri = namespacer.process(querySolution.getResource(vars[0]).getURI());
				Entity entity = new Entity(resultUri);

				// higher weight for categories
				if (resultUri.startsWith("cat")) {
					for (int i = 0; i < CATEGORY_WEIGHT - 1; i++) {
						Entity meta_entity = new Entity(resultUri + i);
						meta_entity.setMeta(true);
						entities.add(meta_entity);

					}
				}
				// skip file links
				if (resultUri.startsWith("file")) {
					continue;
				}

				entities.add(entity);

			}
		}
		return entities;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Map<Entity, Map<Entity, Double>> calculatedEntitiesMatrix2 = getCalculatedEntitiesMatrix();
		for (Entry<Entity, Map<Entity, Double>> entry : calculatedEntitiesMatrix2.entrySet()) {
			Entity entity = entry.getKey();
			Map<Entity, Double> similarityMap = entry.getValue();
			for (Entry<Entity, Double> similarityEntry : similarityMap.entrySet()) {

				Double similarity = similarityEntry.getValue();
				if (similarity > 0.0) {
					String str = entity + "\t" + similarityEntry.getKey() + "\t" + similarity + "\n";
					builder.append(str);
				}
			}
		}

		return builder.toString();
	}

	public static class EntityTuple {
		public Entity first = new Entity();
		public Entity second = new Entity();
		private Double similarityValue = 0.0;
		// private int similarityRank;
		private String attributeType;
		private Double attributeWeight;

		public EntityTuple(Entity x, Entity y) {
			this(x, y, false);
		}

		private EntityTuple(Entity x, Entity y, boolean isAntiSymmetric) {
			if (isAntiSymmetric && x.compareTo(y) > 0) {
				this.first = y;
				this.second = x;
			} else {
				this.first = x;
				this.second = y;
			}
		}

		public void setSimilarityValue(Double similarityValue) {
			if (similarityValue == null) {
				similarityValue = 0.0;
			}
			// else if(similarityValue == 1.0){
			// similarityValue = 2.0;
			// }
			this.similarityValue = similarityValue;
		}

		public Double getSimilarityValue() {
			return similarityValue;
		}

		@Override
		public String toString() {

			return "(" + first + "," + second + " attr: " + attributeType + " sim: "
					+ StringUtils.doubleToString(getSimilarityValue()) + ")";
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}
			if (!(other instanceof EntityTuple)) {
				return false;
			}
			EntityTuple other_ = (EntityTuple) other;
			return other_.first.equals(this.first) && other_.second.equals(this.second);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((first == null) ? 0 : first.hashCode());
			result = prime * result + ((second == null) ? 0 : second.hashCode());
			return result;
		}

		public void setAttributeType(String attributeType) {
			this.attributeType = attributeType;
		}

		public String getAttributeType() {
			return attributeType;
		}

		public void setAttributeWeight(Double attributeWeight) {
			this.attributeWeight = attributeWeight;
		}

		public Double getAttributeWeight() {
			return attributeWeight;
		}
	}
}