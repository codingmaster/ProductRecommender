package de.hpi.semrecsys.similarity.category;

import java.util.Collection;
import java.util.Set;

import de.hpi.semrecsys.model.Category;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.utils.CollectionUtils;

public class CategoryInformationContentSimilarityCalculator extends CategorySimilarityCalculator {

	/**
	 * Calculates similarity based on information content of categories. The
	 * method is described in
	 * "Using Information Content to Evaluate Semantic Similarity in a Taxonomy"
	 * Philip Resnik 1. calculation of category frequency (how many products are
	 * in category) 2. calculation of category probability frequency/all
	 * products 3. calculation of weight = sum(-log(prob)) 4. Similairty =
	 * intersectionWeight/unionWeight
	 * 
	 * @param product1
	 * @param product2
	 * @return
	 */
	public Double calculateSimilarity(Product product1, Product product2) {
		Set<Category> categories1 = productCategoryManager.findCategoriesForProduct(product1);
		Set<Category> categories2 = productCategoryManager.findCategoriesForProduct(product2);

		Collection<? extends Object> intersection = CollectionUtils.getIntersection(categories1, categories2);
		Collection<? extends Object> union = CollectionUtils.getUnion(categories1, categories2);
		logger.debug("\nIntersection: ");
		Double intersectSimilarity = calculateWeightSum(intersection);

		logger.debug("\nUnion:");
		Double unionSimilarity = calculateWeightSum(union);
		Double similarity = intersectSimilarity / unionSimilarity;
		return similarity;
	}

	private Double calculateWeightSum(Collection<? extends Object> collection) {
		Double similarity = 0.0;
		for (Object obj : collection) {
			Category intersectCategory = (Category) obj;
			Double weight = getCategoryWeight(intersectCategory);
			similarity += weight;
			logger.debug(intersectCategory + " weight: " + weight);
		}
		return similarity;
	}

	protected Double getCategoryWeight(Category category) {
		Double categoryWeight = -1 * Math.log(category.getCategoryProbability());
		return categoryWeight;
	}

}
