package de.hpi.semrecsys.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hpi.semrecsys.persistence.CategoryDAO;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute.AttributeType;
import de.hpi.semrecsys.model.Category;
import de.hpi.semrecsys.utils.CollectionUtils;


/**
 * creates meta information RDF triples
 *
 * such as attribute weights
 */
public class MetaTriplesCreator extends EntitySimilarityTriplesCreator {

	Logger log = Logger.getLogger(getClass());
	private AttributeType[] attributeTypes = { AttributeType.struct, AttributeType.unstruct, AttributeType.split,
			AttributeType.cat, AttributeType.img };

	public MetaTriplesCreator(SemRecSysConfigurator configurator) {
		super(configurator);
	}

	@Override
	public List<Triple> createTriples(int number) {
		List<Triple> triples = new ArrayList<Triple>();
		List<Triple> attributeWeightTriples = createAttributeWeightsTriples(attributeTypes);
		triples.addAll(attributeWeightTriples);
		return triples;
	}

	public void setAttributeTypes(AttributeType[] attributeTypes) {
		this.attributeTypes = attributeTypes;

	}

	protected List<Triple> createAttributeWeightsTriples(AttributeType[] types) {
		List<Triple> triples = new ArrayList<Triple>();
		Map<String, Double> attributeWeights = configurator.getJsonProperties().getAttributesByType(types);
		log.info("Attribute weights:\n" + CollectionUtils.mapToString(attributeWeights));

		for (Entry<String, Double> attributeWeight : attributeWeights.entrySet()) {
			Triple triple = createAttributeWeightTriple(attributeWeight.getKey(), attributeWeight.getValue());
			triples.add(triple);
		}
		return triples;
	}

	protected List<Triple> createCategoryHierarchyTriples() {
		Map<Integer, Category> categories = CategoryDAO.getCategories();
		List<Triple> triples = new ArrayList<Triple>();

		for (Category category : categories.values()) {
			if (category.getParent() != null) {
				Triple triple = createCategoryHierarchyTriple(category);
				triples.add(triple);
			}
		}

		return triples;
	}

	private Triple createCategoryHierarchyTriple(Category category) {
		Node s = createCategoryNode(category);

		Node p = createPropertyNode(Property.categoryParent);

		String parentUri = configurator.getVirtuosoBaseGraph() + CATEGORY_PREFIX + category.getParent().getValue();
		Node o = Node.createURI(parentUri);
		return new Triple(s, p, o);
	}

	private Triple createAttributeWeightTriple(String attribute, Double weight) {
		Node s = createPropertyNode(attribute);
		Node prop = createPropertyNode(Property.attribute_weight);

		LiteralLabel literal = LiteralLabelFactory.create(weight);
		Node o = Node.createLiteral(literal);
		Triple triple = new Triple(s, prop, o);
		return triple;
	}

}
