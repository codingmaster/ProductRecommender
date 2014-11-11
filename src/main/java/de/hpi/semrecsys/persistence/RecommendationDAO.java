package de.hpi.semrecsys.persistence;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.hpi.semrecsys.Recommendation;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.strategy.RecommendationValidator;


/**
 * Database functionality for category {@link de.hpi.semrecsys.Recommendation}
 */
public abstract class RecommendationDAO extends AbstractDAO {

	public List<Recommendation> getRecommendations(Product product, int maxResults) {

		List<Recommendation> recommendations = getValidRecommendations(product, maxResults, 0);
		int offset = 0;
		offset = getValidRecommendations(product, maxResults, recommendations, maxResults);
		offset = getValidRecommendations(product, maxResults, recommendations, offset);
		offset = getValidRecommendations(product, maxResults, recommendations, offset);

		return recommendations;
	}

	private int getValidRecommendations(Product product, int maxResults, List<Recommendation> recommendations,
			int offset) {
		int invalidRecommendations;
		int numberOfRecommendations;
		invalidRecommendations = maxResults - recommendations.size();
		if (invalidRecommendations > 0) {
			numberOfRecommendations = invalidRecommendations;
			offset += invalidRecommendations;
			recommendations.addAll(getValidRecommendations(product, numberOfRecommendations, offset));
		}
		return offset;
	}

	private List<Recommendation> getValidRecommendations(Product product, int numberOfRecommendations, int offset) {
		List<Recommendation> newRecommendations = getRecommendations(product.getProductId(), getType(),
				numberOfRecommendations, offset);
		newRecommendations = getValidRecommendations(newRecommendations);
		return newRecommendations;
	}

	private List<Recommendation> getValidRecommendations(List<Recommendation> recommendations) {
		List<Recommendation> validRecommendations = new ArrayList<Recommendation>();
		for (Recommendation recommendation : recommendations) {
			Product recommendationProduct = ProductDAO.getDefault().findById(recommendation.getId().getLinkedProductId());

			if (RecommendationValidator.isValidRecommendation(recommendationProduct)) {
				validRecommendations.add(recommendation);
			} else {
				System.err.println("WARN: Recommendation " + recommendation + " is invalid. Trying another one");
			}

		}
		return validRecommendations;
	}

	protected List<Recommendation> getRecommendations(int productId, Class<?> type, int maxResults, int firstResult) {
		Session session = getSession();
		Criteria criteria = session.createCriteria(type);
		criteria.add(Restrictions.eq("id.productId", productId));
		criteria.add(Restrictions.eq("id.DTYPE", type.getName()));
		criteria.addOrder(Order.asc("id.position"));
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);
		@SuppressWarnings("unchecked")
		List<Recommendation> result = criteria.list();
		return result;
	}

}
