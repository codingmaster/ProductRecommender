package de.hpi.semrecsys.similarity.category;

import java.util.Collection;
import java.util.Set;

import de.hpi.semrecsys.model.Category;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.utils.CollectionUtils;

public class CategoryProbabilitySimilarityCalculator extends CategorySimilarityCalculator {

	public Double calculateSimilarity(Product product1, Product product2) {
		Set<Category> categories1 = productCategoryManager.findCategoriesForProduct(product1);
		Set<Category> categories2 = productCategoryManager.findCategoriesForProduct(product2);

		Collection<? extends Object> intersection = CollectionUtils.getIntersection(categories1, categories2);
		Collection<? extends Object> union = CollectionUtils.getUnion(categories1, categories2);
		logger.debug("\nIntersection: ");
		Double intersectSimilarity = calculateProbabilitySum(intersection);

		logger.debug("\nUnion:");
		Double unionSimilarity = calculateProbabilitySum(union);
		Double similarity = intersectSimilarity / unionSimilarity;
		return similarity;
	}

	private Double calculateProbabilitySum(Collection<? extends Object> intersection) {
		Double similarity = 0.0;
		for (Object obj : intersection) {
			Category intersectCategory = (Category) obj;
			Double probability = 1 - intersectCategory.getCategoryProbability();
			similarity += probability;
			logger.debug(intersectCategory + " probability: " + probability);
		}
		return similarity;
	}

}
