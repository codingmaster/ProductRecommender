package de.hpi.semrecsys.persistence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.hpi.semrecsys.GeneratedRecommendation;

public class GeneratedRecommendationDAO extends RecommendationDAO {

	private static GeneratedRecommendationDAO self;
    public static String type;

	@Override
	protected Class<GeneratedRecommendation> getType() {
		return GeneratedRecommendation.class;
	}

	public static GeneratedRecommendationDAO getDefault() {
		if (self == null) {
			self = new GeneratedRecommendationDAO();
		}
		return self;
	}




}
