//package de.hpi.semrecsys.simentity;
//
//import java.security.InvalidParameterException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.query.ResultSetFormatter;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//
//import de.hpi.semrecsys.config.SemRecSysConfigurator;
//import de.hpi.semrecsys.config.SemRecSysConfigurator.Customer;
//import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;
//import de.hpi.semrecsys.model.Entity;
//import de.hpi.semrecsys.utils.CollectionUtils;
//import de.hpi.semrecsys.utils.FileUtils;
//import de.hpi.semrecsys.webservice.SparqlEndpointConnector;
//
///**
// * calculate entity similarity using entity properties
// */
//public class PropertyBasedEntitySimilarityCalculator implements EntitySimilarityCalculator {
//
//	Map<LanguageCode, Map<String, Integer>> propertySizesMaps = new HashMap<LanguageCode, Map<String, Integer>>();
//
//	Logger log = Logger.getLogger(getClass());
//
//	private SemRecSysConfigurator configurator;
//
//	public PropertyBasedEntitySimilarityCalculator(SemRecSysConfigurator configurator) {
//		this.configurator = configurator;
//	}
//
//	@Override
//	public Double calculateSimilarity(Entity entity1, Entity entity2) {
//		return calculateCommonValues(entity1, entity2);
//	}
//
//	protected Double calculateCommonValues(Entity entity1, Entity entity2) {
//		Set<PropertyWeight> propertyWeights1 = getPropertyWeights(entity1);
//		Set<PropertyWeight> propertyWeights2 = getPropertyWeights(entity2);
//		@SuppressWarnings("unchecked")
//		Set<PropertyWeight> commonProperties = (Set<PropertyWeight>) CollectionUtils.getIntersection(propertyWeights1,
//				propertyWeights2);
//
//		Double propertyWeights1Value = calculatePropertiesValue(propertyWeights1);
//		Double propertyWeights2Value = calculatePropertiesValue(propertyWeights2);
//		Double commmonPropertiesValue = calculatePropertiesValue(commonProperties);
//
//		Double dice = 2 * commmonPropertiesValue / (propertyWeights1Value + propertyWeights2Value);
//		return dice;
//	}
//
//	private Double calculatePropertiesValue(Set<PropertyWeight> propertyWeights) {
//		Double value = 0.0;
//		for (PropertyWeight propertyWeight : propertyWeights) {
//			value += propertyWeight.getCalculatedWeight();
//		}
//		return value;
//	}
//
//	protected Double calculateCommonValuesEN(Entity entity1, Entity entity2) {
//		Double similarity = 0.0;
//		LanguageCode language = LanguageCode.EN;
//		SemRecSysConfigurator configurator = SemRecSysConfigurator.getDefaultConfigurator(Customer.melovely, language);
//
//		String enQuery = getCommonValuesQuery(entity1.getLongUri(configurator.getNamespacer()),
//				entity2.getLongUri(configurator.getNamespacer()));
//		log.trace("Executing query: \n" + enQuery);
//
//		ResultSet resultSet = SparqlEndpointConnector.executeQuery(enQuery, configurator.getDbpediaSparqlEndpoint());
//		similarity += processCommonValuesResult(resultSet, language);
//		return similarity;
//	}
//
//	protected Double calculateCommonValuesDE(Entity entity) {
//		String query = getCommonValuesQuery(entity);
//		Double result = executeCommonValuesDE(query);
//		log.debug("Value for entity " + entity + " : " + result);
//		return result;
//	}
//
//	protected Set<PropertyWeight> getPropertyWeights(Entity entity) {
//		String query = getCommonValuesQuery(entity);
//		ResultSet resultSet = SparqlEndpointConnector.executeQuery(query, configurator.getDbpediaSparqlEndpoint());
//		Set<PropertyWeight> propertyWeights = getPropertyWeights(resultSet, LanguageCode.DE);
//		return propertyWeights;
//	}
//
//	private Double executeCommonValuesDE(String query) {
//		ResultSet resultSet = SparqlEndpointConnector.executeQuery(query, configurator.getDbpediaSparqlEndpoint());
//		return processCommonValuesResult(resultSet, configurator.getLanguageCode());
//	}
//
//	private Set<PropertyWeight> getPropertyWeights(ResultSet resultSet, LanguageCode language) {
//		Set<PropertyWeight> propertyWeights = new HashSet<PropertyWeight>();
//		List<QuerySolution> list = ResultSetFormatter.toList(resultSet);
//		for (QuerySolution querySolution : list) {
//
//			PropertyWeight propertyWeight = getPropertyWeight(querySolution, language);
//			log.debug(propertyWeight);
//
//			propertyWeights.add(propertyWeight);
//		}
//		return propertyWeights;
//	}
//
//	private Double processCommonValuesResult(ResultSet resultSet, LanguageCode language) {
//		Double similarity = 0.0;
//		List<QuerySolution> list = ResultSetFormatter.toList(resultSet);
//		for (QuerySolution querySolution : list) {
//
//			PropertyWeight propertyWeight = getPropertyWeight(querySolution, language);
//			log.debug(propertyWeight);
//
//			similarity += propertyWeight.getCalculatedWeight();
//		}
//		return similarity;
//	}
//
//
//	private String getCommonValuesQuery(String entityUri1, String entityUri2) {
//		return "SELECT ?p ?result (count(?result) as ?count) " + "WHERE { " + "<" + entityUri1 + "> ?p ?result ." + "<"
//				+ entityUri2 + "> ?p ?result . " + "_:x ?p ?result. " + "} " + " group by ?p ?result "
//				+ "HAVING (COUNT(?result) > 1) " + " order by ?count";
//	}
//
//	private String getCommonValuesQuery(Entity entity) {
//		String entityUri1 = entity.getLongUri(configurator.getNamespacer());
//		return "SELECT ?p ?result (count(?result) as ?count) WHERE " + "{ " + "<" + entityUri1 + "> ?p ?result . "
//				+ "_:x ?p ?result. " + "} group by ?p ?result " + "HAVING (COUNT(?result) > 1) " + "order by ?count";
//	}
//
//	private PropertyWeight getPropertyWeight(QuerySolution querySolution, LanguageCode language) {
//		PropertyWeight propertyWeight = new PropertyWeight();
//		Iterator<String> varNames = querySolution.varNames();
//		PropertyWeight.setNUMBER_OF_TRIPLES(configurator.getNumberOfDbpediaTriples());
//		while (varNames.hasNext()) {
//			String varNameString = varNames.next();
//			QueryVarName varName = QueryVarName.valueOf(varNameString);
//			RDFNode varValue = querySolution.get(varNameString);
//
//			switch (varName) {
//			case p:
//				String propertyType = varValue.asNode().getURI();
//				propertyWeight.setPropertyType(propertyType);
//				Integer propertyTypeWeight = getPropertySizesMap(language).get(propertyType);
//				propertyWeight.setPropertyTypeWeight(propertyTypeWeight);
//				break;
//			case count:
//				Integer count = (Integer) varValue.asNode().getLiteral().getValue();
//				propertyWeight.setPropertyValueWeight(count);
//				break;
//			case result:
//				String propertyValue = varValue.asNode().toString();
//				propertyWeight.setPropertyValue(propertyValue);
//				break;
//			default:
//				throw new InvalidParameterException("Parameter " + varName + " should not be in result set");
//			}
//		}
//		return propertyWeight;
//	}
//
//	private Map<String, Integer> getPropertySizesMap(LanguageCode language) {
//		Map<String, Integer> propertySizeMap = propertySizesMaps.get(language);
//		if (propertySizeMap == null) {
//			List<String> lines = FileUtils.readTextFromFileToLines(configurator.getPropertySizesFileName());
//
//			propertySizeMap = new HashMap<String, Integer>();
//			int idx = 0;
//			for (String line : lines) {
//				if (idx > 0) {
//					String[] parts = line.split("\t");
//					String property = parts[0].replace("\"", "");
//					Integer number = Integer.valueOf(parts[1]);
//					propertySizeMap.put(property, number);
//
//				}
//				idx++;
//			}
//			propertySizesMaps.put(language, propertySizeMap);
//		}
//		return propertySizeMap;
//	}
//
//	public void setLogLevel(Level level) {
//		log.setLevel(level);
//
//	}
//
//	public enum QueryVarName {
//		p, result, count;
//	}
//
//}
