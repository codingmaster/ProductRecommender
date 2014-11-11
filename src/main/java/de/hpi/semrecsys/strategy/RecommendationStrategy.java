package de.hpi.semrecsys.strategy;

import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.output.RecommendationResultsHolder;

/**
 * Interface for recommendation strategy defines steps necessary for the recommendations.
 * @author Michael Wolowyk
 *
 */
public interface RecommendationStrategy {
	RecommendationResultsHolder getRecommendationResults(Product product, String type);
    RecommendationResultsHolder getSimilarityBetweenProducts(Product product1, Product product2);

}
