package de.hpi.semrecsys.output;

import java.util.*;

import org.apache.log4j.Logger;

import de.hpi.semrecsys.GeneratedRecommendation;
import de.hpi.semrecsys.Recommendation;
import de.hpi.semrecsys.RecommendationId;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix.EntityTuple;
import de.hpi.semrecsys.utils.StringUtils;

public class RecommendationResult extends GeneratedRecommendation implements Recommendation,
		Comparable<RecommendationResult> {

	List<EntityTuple> commonEntities = new ArrayList<EntityTuple>();
	Map<String, List<EntityTuple>> commonEntitiesMap;
	Logger log = Logger.getLogger(getClass());
	Product baseProduct;
	Product recommendedProduct;
	String calculationResultString;

	public RecommendationResult(Product baseProduct, Product recommendedProduct, int position) {
		super();
		this.baseProduct = baseProduct;
		this.recommendedProduct = recommendedProduct;
		RecommendationId similarProductId = new RecommendationId(baseProduct.getProductId(),  position, recommendedProduct.getProductId());
		setId(similarProductId);
	}

	public RecommendationResult(Product baseProduct, Product recommendedProduct) {
		this(baseProduct, recommendedProduct, -1);
	}

	public List<EntityTuple> getCommonEntities() {
		return commonEntities;
	}

	public void setCommonEntities(List<EntityTuple> commonEntities) {
		this.commonEntities = commonEntities;
	}

	public void addCommonEntityTuple(EntityTuple tuple) {
		this.commonEntities.add(tuple);
	}

	public Map<String, List<EntityTuple>> getCommonEntitiesMap() {
		if (commonEntitiesMap == null) {
			commonEntitiesMap = new HashMap<String, List<EntityTuple>>();
			for (EntityTuple entityTuple : commonEntities) {
				String attributeType = entityTuple.getAttributeType();
				List<EntityTuple> tuples = commonEntitiesMap.get(attributeType);
				if (tuples == null) {
					tuples = new ArrayList<EntityTuple>();
				}
				tuples.add(entityTuple);
				commonEntitiesMap.put(attributeType, tuples);
			}
		}
		return commonEntitiesMap;
	}

	public Map<Entity, List<EntityTuple>> getCommonEntitiesMapForAttribute(String attributeCode) {
		Map<Entity, List<EntityTuple>> commonEntitiesMap = new HashMap<Entity, List<EntityTuple>>();
		List<EntityTuple> commonEntitiesByAttribute = getCommonEntitiesMap().get(attributeCode);
		Set<Entity> usedEntities = new HashSet<Entity>();
		List<EntityTuple> emptyTuples = new ArrayList<EntityTuple>();
		if (commonEntitiesByAttribute != null) {
			for (EntityTuple entityTuple : commonEntitiesByAttribute) {
				Entity first = entityTuple.first;
				Entity second = entityTuple.second;
				List<EntityTuple> entityTuples = commonEntitiesMap.get(first);

				if (entityTuples == null) {
					entityTuples = new ArrayList<EntityTuple>();
				}

				if (first.isEmpty() && !usedEntities.contains(second)) {
					EntityTuple tuple = createEmptyTuple(entityTuple, second);
					emptyTuples.add(tuple);
					// continue;
				}

				if (!first.isEmpty() || (second.isEmpty() && entityTuples.isEmpty())) {
					entityTuples.add(entityTuple);
					usedEntities.add(second);
				}

				if (second.isEmpty() && entityTuples.size() > 1) {
					entityTuples.remove(entityTuple);
				}

				commonEntitiesMap.put(first, entityTuples);

			}

			commonEntitiesMap.put(new Entity(), emptyTuples);
		}
		return commonEntitiesMap;
	}

	private EntityTuple createEmptyTuple(EntityTuple entityTuple, Entity second) {
		EntityTuple tuple = new EntityTuple(new Entity(), second);
		tuple.setAttributeType(entityTuple.getAttributeType());
		tuple.setAttributeWeight(entityTuple.getAttributeWeight());
		tuple.setSimilarityValue(0.0);
		return tuple;
	}

	public Product getBaseProduct() {
		return baseProduct;
	}

	public Product recommendedProduct() {
		return recommendedProduct;
	}


    public GeneratedRecommendation toRecommendation() {
		GeneratedRecommendation recommendation = new GeneratedRecommendation(getId(), getType());
		recommendation.setRelativeScore(getRelativeScore());
		recommendation.setScore(getScore());
        recommendation.setCreatedAt(new Date());
		return recommendation;
	}

	public String calculationResultString() {
		return calculationResultString;
	}

	@Override
	public String toString() {
		return recommendedProduct() + "\t" + recommendationScoreToString();
	}

    /**
     * calculates product similarity for attribute types
     * @param attributesByType
     * @return
     */
	public Double calculateProductSimilarity(Map<String, Double> attributesByType) {
		relativeScore = 0.0;
		Double productSim = 0.0;
		int attributeWeightSum = 0;

		for (String attributeCode : getCommonEntitiesMap().keySet()) {
			Double attributeWeight = attributesByType.get(attributeCode);
			Map<Entity, List<EntityTuple>> commonEntitiesMap = getCommonEntitiesMapForAttribute(attributeCode);
			Double attributeTupleSim = 0.0;
			int attributeTupleCount = 0;
			for (Entity entity : commonEntitiesMap.keySet()) {
				List<EntityTuple> entityTuples = commonEntitiesMap.get(entity);
				if (entityTuples.isEmpty()) {
					continue;
				}

				if (entity.isEmpty()) {
					attributeTupleCount += entityTuples.size();
				} else {
					attributeTupleCount++;
				}
				Double tupleSim = processEntityTuples(entityTuples);

				attributeTupleSim += tupleSim;
			}

			Double weightedAttributeSim = 0.0;
			if (attributeTupleCount > 0) {
				Double attributeSim = attributeTupleSim / attributeTupleCount;
				weightedAttributeSim = attributeSim * attributeWeight;

			}
			attributeWeightSum += attributeWeight;
			productSim += weightedAttributeSim;
		} // attributeCode

		relativeScore = productSim / attributeWeightSum;

		return relativeScore;
	}

    /**
     * Creates HTML output table for commonEntities with product similarity calculations
     * @param attributesByType
     * @return
     */
	public String commonEntitiesToHTML(Map<String, Double> attributesByType) {
		if (calculationResultString == null) {
			StringBuilder builder = new StringBuilder();
			Double productSim = 0.0;
			int attributeWeightSum = 0;

			StringBuilder productStringBuilder = new StringBuilder("<h3>(");
			for (String attributeCode : getCommonEntitiesMap().keySet()) {
				Double attributeWeight = attributesByType.get(attributeCode);
				builder.append("</br><b>" + attributeCode + "</b>" + "(" + attributeWeight + ")</br>");

				Map<Entity, List<EntityTuple>> commonEntitiesMap = getCommonEntitiesMapForAttribute(attributeCode);

				Double attributeTupleSim = 0.0;
				int attributeTupleCount = 0;
				for (Entity entity : commonEntitiesMap.keySet()) {
					List<EntityTuple> entityTuples = commonEntitiesMap.get(entity);
					if (entityTuples.isEmpty()) {
						continue;
					}

					if (entity.isEmpty()) {
						attributeTupleCount += entityTuples.size();
					} else {
						attributeTupleCount++;
					}
					builder.append(HTMLOutputCreator.entityToLink(entity));
					Double tupleSim = processEntityTuples(builder, entityTuples);

					attributeTupleSim += tupleSim;

				}

				Double weightedAttributeSim = 0.0;
				if (attributeTupleCount > 0) {
					Double attributeSim = attributeTupleSim / attributeTupleCount;
					weightedAttributeSim = attributeSim * attributeWeight;
					builder.append("<hr>" + StringUtils.equationToString(attributeTupleSim, attributeTupleCount, "/")
							+ " = " + StringUtils.doubleToString(attributeSim));
					productStringBuilder.append(StringUtils.equationToString(attributeWeight, attributeSim, "*"))
							.append(" + ");
				}

				attributeWeightSum += attributeWeight;
				productSim += weightedAttributeSim;
				builder.append("</br>");

			} // attributeCode

			relativeScore = productSim / attributeWeightSum;
			productStringBuilder.append(") / ").append(attributeWeightSum).append(" = ")
					.append(StringUtils.doubleToString(relativeScore) + "</h3>");
			builder.append("<hr><hr>");
			builder.append(productStringBuilder);
			calculationResultString = builder.toString();
		}

		return calculationResultString;
	}

	private Double processEntityTuples(StringBuilder builder, List<EntityTuple> entityTuples) {
		int idx = 0;
		Double tupleSim = 0.0;
		for (EntityTuple entityTuple : entityTuples) {
			int count1 = entityTuple.first.getCount();
			int count2 = entityTuple.second.getCount();
			if (idx == 0) {
				if (count1 > 1) {
					builder.append(" *" + count1);
				}
				builder.append("&nbsp;&nbsp;{ ");
			}

			tupleSim += entityTuple.getSimilarityValue() * (count1 + count2) / 2;
			builder.append(entityTupleToString(entityTuple));
			idx++;
		}
		builder.append("}");
		builder.append(" = ").append(StringUtils.termToString(tupleSim));
		if (tupleSim > 1.0) {
			tupleSim = 1.0;
			builder.append(" &gt; " + tupleSim);
		}

		builder.append("</br>");
		return tupleSim;
	}

	private Double processEntityTuples(List<EntityTuple> entityTuples) {
		Double tupleSim = 0.0;
		for (EntityTuple entityTuple : entityTuples) {
			int count1 = entityTuple.first.getCount();
			int count2 = entityTuple.second.getCount();
			tupleSim += entityTuple.getSimilarityValue() * (count1 + count2) / 2;
		}
		if (tupleSim > 1.0) {
			tupleSim = 1.0;
		}
		return tupleSim;
	}

	private String entityTupleToString(EntityTuple entityTuple) {
		StringBuilder builder = new StringBuilder();
		Entity secondEntity = entityTuple.second;

		String secondEntityString = HTMLOutputCreator.entityToLink(secondEntity);

		Integer count2 = secondEntity.getCount();

		if (count2 > 1) {
			secondEntityString += " *" + count2;
		}
		builder.append(secondEntityString + " = " + StringUtils.doubleToString(entityTuple.getSimilarityValue()) + "; ");
		return builder.toString();
	}

	@Override
	public int compareTo(RecommendationResult o) {
		if (o.getRelativeScore() >= this.getRelativeScore()) {
			return 1;
		} else {
			return -1;
		}
	}

}
