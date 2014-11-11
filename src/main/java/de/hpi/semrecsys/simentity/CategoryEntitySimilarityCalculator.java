package de.hpi.semrecsys.simentity;

import java.util.Set;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.simentity.WikipageLinksEntitySimilarityCalculator.SimilarityCalculationType;
import de.hpi.semrecsys.utils.CollectionUtils;

/**
 * calculate entity similarity using taxonomical structure
 */
public class CategoryEntitySimilarityCalculator extends WikipageLinksEntitySimilarityCalculator implements
		EntitySimilarityCalculator {
	
	private int maxDepth = 1;
	private double coeff0 = 0.7;
	private double coeff1 = 0.3;
	private double coeff2 = 0.2;
	private double approachCoeff = 0.5;
    private static CategoryEntitySimilarityCalculator instance;


    protected CategoryEntitySimilarityCalculator(SemRecSysConfigurator configurator) {
        super(configurator);
	}

//	public CategoryEntitySimilarityCalculator(SemRecSysConfigurator configurator, int depth) {
//		super(configurator, depth);
//	}

    public static CategoryEntitySimilarityCalculator getDefault(SemRecSysConfigurator configurator){
        if(instance == null){
            instance = new CategoryEntitySimilarityCalculator(
                    configurator);
        }
        return instance;
    }

	@Override
	public Double calculateSimilarity(Entity entity1, Entity entity2) {
		return super.calculateSimilarity(entity1, entity2);
	}
	
	


	@Override
	public String getQuery(int depth, String uri, String... vars) {
		String sparqlQueryString1 = null;
		if (depth == 0) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE { " + "<" + uri
					+ "> <http://purl.org/dc/terms/subject> ?" + vars[0] + " . " + "} ";
		} else if (depth == 1) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " ?" + vars[1] + " WHERE { " + "<" + uri
					+ "> <http://purl.org/dc/terms/subject> ?" + vars[1] + ". " + "?" + vars[1] + " ?p ?" + vars[0]
					+ " . " + "FILTER isURI(?" + vars[0] + " )" + "} ";
		} else if (depth == 2) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE " + "{ " + "<" + uri
					+ "> <http://purl.org/dc/terms/subject> ?" + vars[1] + ". " + "?o ?p ?o2 . " + "?o2 ?p ?" + vars[0]
					+ " . " + "FILTER isURI(?" + vars[0] + " )" + "} ";
		} else if (depth == 3) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE " + "{ " + "<" + uri
					+ "> <http://purl.org/dc/terms/subject> ?" + vars[1] + ". " + "?o ?p ?o2 . " + "?o2 ?p ?o3 . "
					+ "?o3 ?p ?" + vars[0] + " . " + "FILTER isURI(?" + vars[0] + " )" + "} ";
		}
		return sparqlQueryString1;
	}

	

	
	@Override
	public Double getApproachCoeff() {
		return approachCoeff;
	}
	
	@Override
	public Double getCoeff(int depth) {
		if(depth == 0){
			return coeff0;
		}
		if(depth == 1){
			return coeff1;
		}
		if(depth == 2){
			return coeff2;
		}
		return 0.0;
	}
	
	@Override
	public Integer getMaxDepth() {
		return maxDepth;
	}

}
