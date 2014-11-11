package de.hpi.semrecsys.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.utils.CollectionUtils;

public class ProductSimilarity {

	private final Product initialProduct;
	private List<AttributeEntityMapping> attributeEntityMapping = new ArrayList<AttributeEntityMapping>();
	private Map<Product, Double> similarProducts = new HashMap<Product, Double>();

	public ProductSimilarity(Product product) {
		this.initialProduct = product;
	}

	public void addSimilarProduct(Product product, Double value) {
		similarProducts.put(product, value);

	}

	public Map<Product, Double> getSimilarProducts() {
		return similarProducts;
	}

	public Product getInitialProduct() {
		return initialProduct;
	}

	public List<AttributeEntityMapping> getAttributeEntityMappings() {
		return attributeEntityMapping;
	}

	public void addAttributeEntityMapping(AttributeEntityMapping AttributeEntityMapping) {
		attributeEntityMapping.add(AttributeEntityMapping);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Base: " + initialProduct + "\n");
		Map<Product, Double> sortedMap = CollectionUtils.sortByValueAsc(getSimilarProducts());
		builder.append(CollectionUtils.mapToString(sortedMap) + "\n");
		return builder.toString();
	}

	

}
