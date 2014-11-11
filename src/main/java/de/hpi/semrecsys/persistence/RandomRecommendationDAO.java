package de.hpi.semrecsys.persistence;

import de.hpi.semrecsys.RandomRecommendation;

public class RandomRecommendationDAO extends RecommendationDAO {
	private static RandomRecommendationDAO self;

	@Override
	protected Class<RandomRecommendation> getType() {
		return RandomRecommendation.class;
	}

	public static RandomRecommendationDAO getDefault() {
		if (self == null) {
			self = new RandomRecommendationDAO();
		}
		return self;
	}



}
