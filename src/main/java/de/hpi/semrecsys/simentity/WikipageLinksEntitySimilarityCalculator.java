package de.hpi.semrecsys.simentity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.semrecsys.persistence.EntityDAO;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.utils.CollectionUtils;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.webservice.SparqlEndpointConnector;

/**
 * calculate entity similarity using Wikipage Links
 */
public class WikipageLinksEntitySimilarityCalculator implements EntitySimilarityCalculator {

	private static final int CATEGORY_WEIGHT = 2;
    private static WikipageLinksEntitySimilarityCalculator instance;
    SemRecSysConfigurator configurator;
	private Namespacer namespacer;
	private Map<Integer, Map<Entity, Set<Entity>>> similarEntitiesContainer = new HashMap<Integer, Map<Entity, Set<Entity>>>();
	private int maxDepth = 0;
	private double coeff0 = 1;
	private double coeff1;
	private double approachCoeff = 0.5;
	static Logger log = Logger.getLogger("");
	double REFERENCE_BOOST = 0.2;

	protected WikipageLinksEntitySimilarityCalculator(SemRecSysConfigurator configurator) {
		this.configurator = configurator;
		this.namespacer = configurator.getNamespacer();
	}

    public static WikipageLinksEntitySimilarityCalculator getDefault(SemRecSysConfigurator configurator){
        if(instance == null){
            instance = new WikipageLinksEntitySimilarityCalculator(
                    configurator);
        }
        return instance;
    }

//	public WikipageLinksEntitySimilarityCalculator(SemRecSysConfigurator configurator) {
//		this(configurator);
//	}

	@Override
	public Double calculateSimilarity(Entity entity1, Entity entity2) {
		Double result = 0.0;
		for(int depth = 0; depth <= getMaxDepth(); depth++){
			Set<Entity> linksOut = getEntities(entity1, depth);
			Set<Entity> linksOut2 = getEntities(entity2, depth);
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
	


	public Double getCoeff(int depth) {
		
		if(depth == 0){
			return coeff0;
		}
		if(depth == 1){
			return coeff1;
		}
		return null;
	}

	public Set<Entity> getEntities(Entity entity1, int depth) {
		Set<Entity> entities = getSimilarEntitiesContainer(depth).get(entity1);
		if (entities == null) {
			entities = new HashSet<Entity>();
			String uri = EntityDAO.getLongUri(configurator.getNamespacer(), entity1);
			String[] var = getVars(depth);

			String sparqlQueryString1 = getQuery(depth, uri, var);
			entities = getEntityList(sparqlQueryString1, configurator.getDbpediaSparqlEndpoint(), var);
		}
		getSimilarEntitiesContainer(depth).put(entity1, entities);
		return entities;
	}

	protected String[] getVars(int depth) {
		String[] var = new String[depth + 1];
		var[0] = "result";
		if (depth > 0) {
			var[1] = "parent";
		}
		return var;
	}

	public Map<Entity, Set<Entity>> getSimilarEntitiesContainer(int depth) {
		Map<Entity, Set<Entity>> similarEntities = similarEntitiesContainer.get(depth);
		if (similarEntities == null) {
			similarEntities = new HashMap<Entity, Set<Entity>>();
		}
		similarEntitiesContainer.put(depth, similarEntities);
		return similarEntities;
	}

	public Set<Entity> getWikipageLinksOf(Entity entity1, int depth) {
		String uri = EntityDAO.getLongUri(configurator.getNamespacer(), entity1);
		String var = "result";
		String sparqlQueryString1 = getWikipageLinksQueryOf(depth, uri, var);

		Set<Entity> entities = getEntityList(sparqlQueryString1, configurator.getDbpediaSparqlEndpoint(), var);
		return entities;
	}

	private Set<Entity> getEntityList(String sparqlQueryString1, String endpoint, String... vars) {
		Set<Entity> entities = new HashSet<Entity>();
		ResultSet resultSet = SparqlEndpointConnector.executeQuery(sparqlQueryString1, endpoint);
		if (resultSet != null) {
			List<QuerySolution> results = ResultSetFormatter.toList(resultSet);
			for (QuerySolution querySolution : results) {

				String resultUri = namespacer.process(querySolution.getResource(vars[0]).getURI());
				
				
				
				Entity entity = new Entity(resultUri);

				// higher weight for categories
				if (resultUri.startsWith("cat")) {
					for (int i = 0; i < CATEGORY_WEIGHT - 1; i++) {
						Entity meta_entity = new Entity(resultUri + i);
						meta_entity.setMeta(true);
						entities.add(meta_entity);

					}
		
				}
				// skip file links
				if (resultUri.startsWith("file")) {
					continue;
				}

				if(vars.length == 2 && vars[1] == "number"){
					Integer number = querySolution.getLiteral("number").getInt();
					entity.setCount(number);
				}
				
				entities.add(entity);

			}
		}
		return entities;
	}

	private String getQuery(int depth, String uri, String property, String... vars) {
		String sparqlQueryString1 = null;
		if (depth == 0) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE { " + "<" + uri + "> " + property + " ?"
					+ vars[0] + " . " + "} ";
		} else if (depth == 1) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " ?" + vars[1] + " WHERE { " + "<" + uri + "> "
					+ property + " ?" + vars[1] + ". " + "?" + vars[1] + " " + property + " ?" + vars[0] + " . " + "} ";
		} else if (depth == 2) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE " + "{ " + "<" + uri + "> " + property
					+ " ?o . " + "?o " + property + " ?o2 . " + "?o2 " + property + " ?" + vars[0] + " . " + "} ";
		} else if (depth == 3) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + vars[0] + " WHERE " + "{ " + "<" + uri + "> " + property
					+ " ?o . " + "?o " + property + " ?o2 . " + "?o2 " + property + " ?o3 . " + "?o3 " + property
					+ " ?" + vars[0] + " . " + "} ";
		}
		return sparqlQueryString1;
	}

	protected String getQuery(int depth, String uri, String... vars) {
		String property = "<http://dbpedia.org/ontology/wikiPageWikiLink>";
		return getQuery(depth, uri, property, vars);
	}

	private String getWikipageLinksQueryOf(int depth, String uri, String var) {
		String sparqlQueryString1 = null;
		if (depth == 0) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + var + " WHERE " + "{ " + "?" + var
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink> " + "<" + uri + "> " + " . " + "} ";
		} else if (depth == 1) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + var + " WHERE " + "{ " + " ?o "
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink>" + "<" + uri + "> " + ". " + " ?" + var
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink>" + "?o " + " . " + "} ";
		} else if (depth == 2) {
			sparqlQueryString1 = "SELECT DISTINCT ?" + var + " WHERE " + "{ " + "?o "
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink> " + "<" + uri + "> " + " . " + "?o2  "
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink> " + "?o ." + "?" + var
					+ "<http://dbpedia.org/ontology/wikiPageWikiLink> " + "?o2 " + " . " + "} ";
		}
		return sparqlQueryString1;
	}

	public enum SimilarityCalculationType {
		overlapp, dice, jaccard;
	}

	@Override
	public Double getApproachCoeff() {
		return approachCoeff;
	}
	
	@Override
	public Integer getMaxDepth() {
		return maxDepth;
	}

}
