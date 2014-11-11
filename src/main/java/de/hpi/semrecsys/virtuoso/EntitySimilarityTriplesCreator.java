package de.hpi.semrecsys.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix.EntityTuple;
import de.hpi.semrecsys.utils.CollectionUtils;

/**
 * creates entity similarity RDF triples
 */
public class EntitySimilarityTriplesCreator extends ProductTriplesCreator {

	private static final String ENTITY_SIMILARITY_ID_PREFIX = "/entitySimilarityId#";

	Logger log = Logger.getLogger(getClass());

	public EntitySimilarityTriplesCreator(SemRecSysConfigurator configurator) {
		super(configurator);
	}

	@Override
	public List<Triple> createTriples(int number) {
		List<Triple> triples = new ArrayList<Triple>();

		EntitySimilarityMatrix entitySimilarityMatrix = new EntitySimilarityMatrix(configurator, number);
		Map<Entity, Map<Entity, Double>> similarityMatrix = entitySimilarityMatrix.getCalculatedEntitiesMatrix();
		log.debug(similarityMatrix + "\n");

		triples.addAll(createTriplesForEntitySimilarity(similarityMatrix));
		return triples;
	}

	private List<Triple> createTriplesForEntitySimilarity(Map<Entity, Map<Entity, Double>> similarityMatrix) {
		List<Triple> triples = new ArrayList<Triple>();
		for (Entry<Entity, Map<Entity, Double>> entry : similarityMatrix.entrySet()) {
			Entity entityA = entry.getKey();

			Map<Entity, Double> simMap = CollectionUtils.sortByValueDesc(entry.getValue());

			List<EntityTuple> entityTuples = new ArrayList<EntityTuple>();

			for (Entry<Entity, Double> entry2 : simMap.entrySet()) {
				Entity entityB = entry2.getKey();
				Double similarity = entry2.getValue();
				EntityTuple entityTuple = new EntityTuple(entityA, entityB);
				entityTuple.setSimilarityValue(similarity);
				entityTuples.add(entityTuple);
			}
			triples.addAll(createEntitySimilarityTriples(entityTuples));
		}
		return triples;
	}

	protected List<Triple> createEntitySimilarityTriples(List<EntityTuple> entityTuples) {
		List<Triple> triples = new ArrayList<Triple>();
		for (EntityTuple entityTuple : entityTuples) {
			triples.addAll(createEntitySimilarityTriple(entityTuple));
		}
		return triples;
	}

	protected List<Triple> createEntitySimilarityTriple(EntityTuple entityTuple) {
		List<Triple> triples = new ArrayList<Triple>();

		triples.add(createEntitySimilarityTripleEntityA(entityTuple));
		triples.add(createEntitySimilarityTripleEntityB(entityTuple));
		triples.add(createEntitySimilarityTripleSimilarityValue(entityTuple));
		if (entityTuple.getAttributeType() != null) {
			triples.add(createEntitySimilarityTripleAttributeCode(entityTuple));
		}
		return triples;
	}

	// http://localhost:8890/melovely/prop#style
	private Triple createEntitySimilarityTripleAttributeCode(EntityTuple entityTuple) {
		Integer entitySimilarityId = entityTuple.hashCode();

		Node entitySimilarityIdNode = Node.createURI(getGraphName() + ENTITY_SIMILARITY_ID_PREFIX + entitySimilarityId);
		Node prop = createPropertyNode(Property.attribute);
		Node entityAttributeNode = createPropertyNode(entityTuple.getAttributeType());

		return new Triple(entitySimilarityIdNode, prop, entityAttributeNode);
	}

	private Triple createEntitySimilarityTripleEntityA(EntityTuple entityTuple) {
		Integer entitySimilarityId = entityTuple.hashCode();

		Node entitySimilarityIdNode = Node.createURI(getGraphName() + ENTITY_SIMILARITY_ID_PREFIX + entitySimilarityId);
		Node prop = createPropertyNode(Property.sim_entity1);
		Node entityANode = createEntityNode(entityTuple.first);
		return new Triple(entitySimilarityIdNode, prop, entityANode);
	}

	private Triple createEntitySimilarityTripleEntityB(EntityTuple entityTuple) {
		Integer entitySimilarityId = entityTuple.hashCode();

		Node entitySimilarityIdNode = Node.createURI(getGraphName() + ENTITY_SIMILARITY_ID_PREFIX + entitySimilarityId);
		Node prop = createPropertyNode(Property.sim_entity2);
		Node entityANode = createEntityNode(entityTuple.second);
		return new Triple(entitySimilarityIdNode, prop, entityANode);
	}

	private Triple createEntitySimilarityTripleSimilarityValue(EntityTuple entityTuple) {
		Integer entitySimilarityId = entityTuple.hashCode();

		Node entitySimilarityIdNode = Node.createURI(getGraphName() + ENTITY_SIMILARITY_ID_PREFIX + entitySimilarityId);
		Node prop = createPropertyNode(Property.sim_value);
		LiteralLabel literal = LiteralLabelFactory.create(entityTuple.getSimilarityValue());
		Node similarityNode = Node.createLiteral(literal);
		return new Triple(entitySimilarityIdNode, prop, similarityNode);
	}

}
