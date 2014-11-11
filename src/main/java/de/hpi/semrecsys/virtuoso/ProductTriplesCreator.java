package de.hpi.semrecsys.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Category;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.populator.EntityFinder;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.spotlight.SpotlightResponse.ResponseResource;
import de.hpi.semrecsys.utils.Namespacer;

/**
 * Creates RDF triples for product
 */
public class ProductTriplesCreator extends AbstractTriplesCreator {

	private final String graphName;
	private final Namespacer namespacer;
	private final String lang;
	protected final SemRecSysConfigurator configurator;
	private final ProductDAO productManager = ProductDAO.getDefault();
	protected final Log log = LogFactory.getLog(getClass());

	protected static final String PROPERTY_PREFIX = "/prop#";
	protected static final String ATTRIBUTE_PREFIX = "/attribute#";
	private static final String VALUE_PREFIX = "/value#";
	protected static final String CATEGORY_PREFIX = "/category/";
	private static final String ATTRIBUTE_ENTITY_ID_PREFIX = "/attributeEntityId#";
	public static String PRODUCT_URI_BASE = "http://www.naturideen.de/catalog/product/view/id/";


    /**
     * create maximal number triples for all products from minProductId to maxProductId
     * @param number maximal number
     * @return list of created triples
     */
	@Override
	public List<Triple> createTriples(int number) {
		List<Triple> triples = new ArrayList<Triple>();
		int maxId =  productManager.getMaxId();
		int startId = productManager.getMinId();
		if (number < 1) {
			number = maxId - startId;
		}
		for (Integer i = startId; i < number + startId; i++) {
			if (i > maxId) {
				break;
			}
			Product product = productManager.findById(i);
			if (product != null && product.getTitle() != null) {
				try {
					triples = createTriplesForProduct(product);
				} catch (Exception ex) {
					log.error("Exception for product " + product + " : " + ex.getMessage());
				}
			}
		}
		return triples;
	}

	/**
	 * Creates triples for Virtuoso datasource
	 * 
	 * @param configurator
	 */
	public ProductTriplesCreator(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.lang = configurator.getLanguageCode().name();
		this.graphName = configurator.getVirtuosoBaseGraph();
		this.namespacer = configurator.getNamespacer();
		PRODUCT_URI_BASE = configurator.getJsonProperties().getCustomerWebsite() + "catalog/product/view/id/";
	}

	public List<Triple> createTriplesForProduct(Product product) {
		EntityFinder creator = new EntityFinder(configurator);
		List<Triple> result = new ArrayList<Triple>();
		AttributeEntityMapping attributeEntityMapping = creator.getAttributeEntityMapping(product);
		result.addAll(createTriplesForAttributeEntityMapping(attributeEntityMapping));
		result.addAll(createCategoryTriplesForProduct(product));
		return result;
	}

	public List<Triple> createCategoryTriplesForProduct(Product product) {
		List<Triple> triples = new ArrayList<Triple>();
		for (Category category : product.getCategories()) {
			Triple triple = createProductCategoryTriple(product, category);
			triples.add(triple);
		}
		return triples;
	}

	private Triple createProductCategoryTriple(Product product, Category category) {
		Node s = createProductNode(product);
		Node p = createPropertyNode(Property.category);
		Node o = createCategoryNode(category);
		return new Triple(s, p, o);
	}

	protected Node createCategoryNode(Category category) {
		String uri = graphName + CATEGORY_PREFIX + category.getValue();
		Node s = Node.createURI(uri);
		return s;
	}

	public List<Triple> createAttributeOptionsTriples(int productId, Map<String, List<String>> optionsMap) {
		List<Triple> triples = new ArrayList<Triple>();
		for (String attributeCode : optionsMap.keySet()) {
			List<String> options = optionsMap.get(attributeCode);
			for (String value : options) {
				triples.add(createAttributeValueTriple(productId, attributeCode, value));
			}
		}
		return triples;
	}

	private Triple createAttributeValueTriple(int productId, String attributeCode, String value) {
		Node s = Node.createURI(PRODUCT_URI_BASE + productId);
		Node p = Node.createURI(getGraphName() + PROPERTY_PREFIX + attributeCode);
		Node o = Node.createURI(getGraphName() + VALUE_PREFIX + value.replace(" ", "_").replace("/", "_"));
		Triple triple = new Triple(s, p, o);
		return triple;

	}

	private List<Triple> createTriplesForAttributeEntityMapping(AttributeEntityMapping attributeEntityMapping) {
		List<Triple> triples = new ArrayList<Triple>();
		Product product = attributeEntityMapping.getProduct();
		Attribute oldAttribute = null;
		for (AttributeEntity attributeEntity : attributeEntityMapping.getAttributeEntities()) {
			Attribute attribute = attributeEntity.getAttribute();
			Integer attributeId = attribute.hashCode();

			if (oldAttribute == null || !oldAttribute.equals(attribute)) {
				triples.add(createProductAttributeTriple(product, attributeId));
				triples.add(createAttributeValueTriple(attribute));
			}

			triples.addAll(createAttributeEntityTriples(attributeEntity));

			oldAttribute = attribute;
		}

		return triples;
	}

	/**
	 * productTriple: product_id prop#attr attr_id
	 * 
	 * @param product
	 * @param attributeId
	 * @return
	 */
	protected Triple createProductAttributeTriple(Product product, Integer attributeId) {
		Node productNode = createProductNode(product);
		Node p = createPropertyNode(Property.prod_attribute);
		Node attrNodeS = Node.createURI(getGraphName() + ATTRIBUTE_PREFIX + attributeId);
		Triple productTriple = new Triple(productNode, p, attrNodeS);
		return productTriple;
	}


    /**
     * attributeValueTriple: attr_id prop#attr_type ""^xsd:string
     * @param attributeType
     * @param attributeValue
     * @return
     */
	protected Triple createAttributeValueTriple(String attributeType, String attributeValue) {
		Triple attrTriple = null;

		if (attributeValue != null && attributeValue.getClass().equals(String.class)) {
			int attributeId = attributeValue.hashCode();

			Node attrNodeS = Node.createURI(getGraphName() + ATTRIBUTE_PREFIX + attributeId);
			Node attrNodeP = Node.createURI(getGraphName() + PROPERTY_PREFIX + attributeType);
			Node attrNodeO = Node.createLiteral(attributeValue, lang, false);

			attrTriple = new Triple(attrNodeS, attrNodeP, attrNodeO);
		}
		return attrTriple;
	}

	protected Triple createAttributeValueTriple(Attribute attribute) {
		Triple attrTriple = null;
		Integer attributeId = attribute.hashCode();
		Node attrNodeS = Node.createURI(getGraphName() + ATTRIBUTE_PREFIX + attributeId);
		Node attrNodeP = Node.createURI(getGraphName() + PROPERTY_PREFIX + attribute.getAttributeCode());
		Node attrNodeO = Node.createLiteral(attribute.getValue(), lang, false);

		attrTriple = new Triple(attrNodeS, attrNodeP, attrNodeO);
		return attrTriple;
	}

	/**
	 * attributeEntityTripleE: attrEntity_id prop#entity entity_id
	 * attributeEntityTripleA: attrEntity_id prop#attr attr_id
	 * attributeEntityTripleWeight: attrEntity_id prop#weight X^xsd:integer
	 * 
	 * @param attributeEntity
	 * @return
	 */
	protected List<Triple> createAttributeEntityTriples(AttributeEntity attributeEntity) {
		List<Triple> triples = new ArrayList<Triple>();
		triples.add(createAttributeEntityTripleA(attributeEntity));
		triples.add(createAttributeEntityTripleWeight(attributeEntity));

		triples.addAll(createAttributeEntityTripleE(attributeEntity));

		return triples;
	}

	/**
	 * attributeEntityTripleWeight: attrEntity_id prop#weight X^xsd:integer
	 * 
	 * @param attributeEntity
	 * @return
	 */
	private Triple createAttributeEntityTripleWeight(AttributeEntity attributeEntity) {
		Integer attributeEntityId = attributeEntity.hashCode();
		Node attributeEntityIdNode = Node.createURI(getGraphName() + ATTRIBUTE_ENTITY_ID_PREFIX + attributeEntityId);
		Node prop = createPropertyNode(Property.entity_weight);
		Integer weight = attributeEntity.getWeight();
		LiteralLabel literal = LiteralLabelFactory.create(weight);
		Node weightNode = Node.createLiteral(literal);
		return new Triple(attributeEntityIdNode, prop, weightNode);
	}

	/**
	 * attributeEntityTripleA: attrEntity_id prop#attr attr_id
	 * 
	 * @param attributeEntity
	 * @return
	 */
	private Triple createAttributeEntityTripleA(AttributeEntity attributeEntity) {
		Integer attributeEntityId = attributeEntity.hashCode();
		Node attributeEntityIdNode = Node.createURI(getGraphName() + ATTRIBUTE_ENTITY_ID_PREFIX + attributeEntityId);
		Node prop = createPropertyNode(Property.entity_attribute);
		Integer attributeId = attributeEntity.getAttribute().hashCode();
		Node attributeIdNode = Node.createURI(getGraphName() + ATTRIBUTE_PREFIX + attributeId);
		return new Triple(attributeEntityIdNode, prop, attributeIdNode);
	}

	/**
	 * attributeEntityTripleE: attrEntity_id prop#entity entity_id
	 * 
	 * @param attributeEntity
	 * @return
	 */
	private List<Triple> createAttributeEntityTripleE(AttributeEntity attributeEntity) {
		List<Triple> triples = new ArrayList<Triple>();
		Integer attributeEntityId = attributeEntity.hashCode();
		Node attributeEntityIdNode = Node.createURI(getGraphName() + ATTRIBUTE_ENTITY_ID_PREFIX + attributeEntityId);
		Node entityProperty = createPropertyNode(Property.entity);
		Entity entity = attributeEntity.getEntity();
		Node entityNode = createEntityNode(entity);
		Triple attributeEntityTripleE = new Triple(attributeEntityIdNode, entityProperty, entityNode);
		triples.add(attributeEntityTripleE);

		for (ResponseResource resource : attributeEntity.getResponseResources()) {
			Node offsetNode = Node.createLiteral(LiteralLabelFactory.create(resource.getOffset()));
			Node offsetProperty = createPropertyNode(Property.offset);
			Triple offsetTriple = new Triple(attributeEntityIdNode, offsetProperty, offsetNode);
			triples.add(offsetTriple);
		}
		// add meta triples if entity is meta
		if (attributeEntity.getEntity().isMeta()) {
			triples.addAll(createAttributeEntityTripleMeta(attributeEntity));
		}
		return triples;
	}

	private List<Triple> createAttributeEntityTripleMeta(AttributeEntity attributeEntity) {
		List<Triple> triples = new ArrayList<Triple>();
		Integer attributeEntityId = attributeEntity.hashCode();
		Node attributeEntityIdNode = Node.createURI(getGraphName() + ATTRIBUTE_ENTITY_ID_PREFIX + attributeEntityId);
		Node entityProperty = createPropertyNode(Property.isMetaEntity);
		Node isMetaNode = Node.createLiteral(LiteralLabelFactory.create(true));
		Triple attributeEntityTripleMeta = new Triple(attributeEntityIdNode, entityProperty, isMetaNode);
		triples.add(attributeEntityTripleMeta);

		return triples;
	}

	public static Integer getProductIdFromUri(String uri) {
		String pattern = "(" + PRODUCT_URI_BASE + ")([0-9]*)([^0-9]*)";
		Integer productId = Integer.valueOf(uri.replaceAll(pattern, "$2"));
		return productId;
	}

	public Node createProductNode(Product product) {
		String uri = PRODUCT_URI_BASE + product.getProductId();
		Node node = Node.createURI(uri);
		return node;
	}

	protected Node createEntityNode(Entity entity) {
		String uri = namespacer.restore(entity.getUri());
		Node node = Node.createURI(uri);
		return node;
	}

	protected Node createPropertyNode(String propName) {
		String uri = getGraphName() + PROPERTY_PREFIX + propName;
		Node node = Node.createURI(uri);
		return node;
	}

	protected Node createPropertyNode(Property prop) {
		String propName = prop.name();
		return createPropertyNode(propName);
	}

	enum Property {
		product, product_name, product_uri, entity, entity_weight, entity_source_text, prod_attribute, entity_attribute, sim_entity1, sim_entity2, sim_overlapp, sim_rank, sim_value, isMetaEntity, attribute_weight, categoryParent, category, offset, attribute
	}

	protected String getGraphName() {
		return graphName;
	}

}
