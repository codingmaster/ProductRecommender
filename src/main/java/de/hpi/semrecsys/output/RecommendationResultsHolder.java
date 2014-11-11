package de.hpi.semrecsys.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.utils.CollectionUtils;

/**
 * container for recommendation results
 */
public class RecommendationResultsHolder {

	Logger log = Logger.getLogger(getClass());
	List<RecommendationResult> recommendationResults = new ArrayList<RecommendationResult>();

	Double baseProductRecommendationScore;
	Product baseProduct;

	public RecommendationResultsHolder(Product baseProduct) {
		this.baseProduct = baseProduct;
	}

	public void setBaseProductRecommendationScore(Double baseProductRecommendationScore) {
		this.baseProductRecommendationScore = baseProductRecommendationScore;
	}

	public Product getBaseProduct() {
		return baseProduct;
	}

	public List<RecommendationResult> getRecommendationResults() {
		return recommendationResults;
	}

    /**
     * add recommendation result to the list
     * @param e
     * @return
     */
	public boolean add(RecommendationResult e) {
		if (baseProductRecommendationScore != null) {
			double relativeRecommendationScore = e.getScore() / baseProductRecommendationScore;
			if (relativeRecommendationScore > 1.0) {
				relativeRecommendationScore = 1.0;
			}
			e.setRelativeScore(relativeRecommendationScore);
		}
		return recommendationResults.add(e);
	}

    /**
     * add recommendation results to the map
     * @param productCountMap
     */
	public void addAll(Map<Product, Double> productCountMap) {
		productCountMap = CollectionUtils.sortByValueDesc(productCountMap);
		int position = 0;
		baseProductRecommendationScore = getBaseRecommendationScore(productCountMap);
		productCountMap.remove(baseProduct);
		for (Entry<Product, Double> recommendationResultEntry : productCountMap.entrySet()) {
			Product recommendedProduct = recommendationResultEntry.getKey();
			RecommendationResult recommendationResult = new RecommendationResult(baseProduct, recommendedProduct,
					position);
			recommendationResult.setScore(recommendationResultEntry.getValue());
			add(recommendationResult);

			position++;
		}
	}

	private Double getBaseRecommendationScore(Map<Product, Double> productCountMap) {
		Double recommendationScore = productCountMap.get(baseProduct);
		if (recommendationScore == null) {
			log.warn(baseProduct + " was not found in the list");
			for (Product product : productCountMap.keySet()) {
				if (product.getProductId() == baseProduct.getProductId()) {
					log.warn("Found: " + product);
					baseProduct = product;
					recommendationScore = productCountMap.get(product);
				}
			}
		}
		return recommendationScore;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (RecommendationResult recommendationResultEntry : recommendationResults) {
			builder.append(recommendationResultEntry + "\n");
		}
		return builder.toString();
	}

    public void remove(Product product) {
        recommendationResults.remove(product);
    }
}
