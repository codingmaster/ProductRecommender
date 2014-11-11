package de.hpi.semrecsys.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.graph.Triple;

import de.hpi.semrecsys.config.AttributeSimilarityMap;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix.EntityTuple;

/**
 * create attribute similarity RDF triples
 */
public class AttributeSimilarityTriplesCreator extends EntitySimilarityTriplesCreator {

	public AttributeSimilarityTriplesCreator(SemRecSysConfigurator configurator) {
		super(configurator);
	}

	@Override
	public List<Triple> createTriples(int number) {
		List<Triple> triples = new ArrayList<Triple>();
		List<EntityTuple> entityTuples = new ArrayList<EntityTuple>();
		Map<String, AttributeSimilarityMap> attributeSimilarityMaps = configurator
				.getCustomAttributeSimilarityCreator().getAttributeSimilarityMaps();
		for (String attributeCode : attributeSimilarityMaps.keySet()) {
			AttributeSimilarityMap attributeSimilarity = attributeSimilarityMaps.get(attributeCode);
			for (Entity entityA : attributeSimilarity.keySet()) {
				Map<? extends Entity, Double> similarEntities = attributeSimilarity.get(entityA);

				for (Entry<? extends Entity, Double> similarEntity : similarEntities.entrySet()) {
					EntityTuple entityTuple = new EntityTuple(entityA, similarEntity.getKey());
					Double similarityValue = similarEntity.getValue();

					entityTuple.setSimilarityValue(similarityValue);
					entityTuple.setAttributeType(attributeCode);
					entityTuples.add(entityTuple);
				}
			}
			triples.addAll(createEntitySimilarityTriples(entityTuples));
		}
		return triples;
	}

}
