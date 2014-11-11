package de.hpi.semrecsys.persistence;

import de.hpi.semrecsys.SystemRecommendation;

public class SystemRecommendationDAO extends RecommendationDAO {

	private static SystemRecommendationDAO self;

	@Override
	protected Class<SystemRecommendation> getType() {
		return SystemRecommendation.class;
	}

	public static SystemRecommendationDAO getDefault() {
		if (self == null) {
			self = new SystemRecommendationDAO();
		}
		return self;
	}


}
