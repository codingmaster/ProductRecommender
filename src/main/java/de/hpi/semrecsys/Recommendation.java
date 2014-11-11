package de.hpi.semrecsys;

/**
 * Interface for Recommendation objects
 * @author Michael Wolowyk
 *
 */
public interface Recommendation extends DBObject {

	public RecommendationId getId();
	public String recommendationScoreToString();

	public boolean empty();



}
