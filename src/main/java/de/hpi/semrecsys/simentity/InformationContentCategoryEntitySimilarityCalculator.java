package de.hpi.semrecsys.simentity;

import java.util.Set;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.simentity.WikipageLinksEntitySimilarityCalculator.SimilarityCalculationType;
import de.hpi.semrecsys.utils.CollectionUtils;



public class InformationContentCategoryEntitySimilarityCalculator extends CategoryEntitySimilarityCalculator implements
EntitySimilarityCalculator{
	private int maxDepth = 1;
	private double coeff0 = 0.7;
	private double coeff1 = 0.3;
	private double coeff2 = 0.2;
	private double approachCoeff = 0.5;
    private static InformationContentCategoryEntitySimilarityCalculator instance;
    public static Integer NUMBER_OF_ELEMENTS = 1585741;
    public static Double MAX_IC = -Math.log((double) (2/NUMBER_OF_ELEMENTS));


    private InformationContentCategoryEntitySimilarityCalculator(SemRecSysConfigurator configurator) {
        super(configurator);
	}

//	public CategoryEntitySimilarityCalculator(SemRecSysConfigurator configurator, int depth) {
//		super(configurator, depth);
//	}

    public static CategoryEntitySimilarityCalculator getDefault(SemRecSysConfigurator configurator){
        if(instance == null){
            instance = new InformationContentCategoryEntitySimilarityCalculator(
                    configurator);
        }
        return instance;
    }

	@Override
	public Double calculateSimilarity(Entity entity1, Entity entity2) {
		return calculateInformationContentSimilarity(entity1, entity2);
	}
	
	private Double calculateInformationContentSimilarity(Entity entity1, Entity entity2) {
		Double result = 0.0;
		for(int depth = 0; depth <= getMaxDepth(); depth++){
			Set<Entity> linksOut = getEntitiesWithInformationContentValues(entity1, depth);
			Set<Entity> linksOut2 = getEntitiesWithInformationContentValues(entity2, depth);
			Double similarity = CollectionUtils.calculateSimilarity(linksOut, linksOut2, SimilarityCalculationType.overlapp);
			
			if(linksOut.contains(entity2)){
				similarity+=REFERENCE_BOOST;
			}
			if(linksOut2.contains(entity1)){
				similarity+=REFERENCE_BOOST;
			}
			
			Double coeff = getCoeff(depth);
			if(similarity > 1.0){
				similarity = 1.0;
			}
			result+=coeff*similarity;
			log.debug("depth: " + depth + " coeff: " + coeff + " sim: " + similarity);
		}
		return result;
	}
	
	private Set<Entity> getEntitiesWithInformationContentValues(Entity entity1, int depth){
		Set<Entity> entities = getEntities(entity1, depth);
		for(Entity entity : entities){
			int count = entity.getCount();
			Double ic = -Math.log((double)(count/NUMBER_OF_ELEMENTS));
			Double relIC = ic/MAX_IC;
			System.out.println(entity + " = " + relIC);
			entity.setRelInformationContent(relIC);
			
		}
		return entities;
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
	
	@Override
	public String getQuery(int depth, String uri, String... vars) {
		String sparqlQueryString1 = null;
		if (depth == 0) {
			sparqlQueryString1 = "SELECT ?" + vars[0] + " (count(?resource) as ?" +vars[1] + " ) WHERE { " 
		+ "<" + uri + "> <http://purl.org/dc/terms/subject> ?" + vars[0] + " . " 
		+ "?resource <http://purl.org/dc/terms/subject> ?" + vars[0] + " . " 
					+ "} "
					+ "group by ?"  + vars[0];
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
}
