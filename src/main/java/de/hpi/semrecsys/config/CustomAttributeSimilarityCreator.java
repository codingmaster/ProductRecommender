package de.hpi.semrecsys.config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.utils.CollectionUtils;
import de.hpi.semrecsys.utils.FileUtils;

/**
 * calculates attribute similarity map for each attribute type
 * @author Michael Wolowyk
 *
 */
public class CustomAttributeSimilarityCreator {

	CustomAttributeSimilarityCreator that;
	Map<String, AttributeSimilarityMap> attributeSimilarityMaps;
	Set<String> singleValueAttributes = new HashSet<String>();

	static int MAX_NUMBER_OF_SIMILAR_ENTITIES = 10;

	Logger log = Logger.getLogger(getClass());

	/**
	 * 
	 * @param customAttributeSimilarityPath path to customer specific attribute similarity values
	 */
	public CustomAttributeSimilarityCreator(String customAttributeSimilarityPath) {
		init(customAttributeSimilarityPath);

	}

	private void init(String fileName) {
		attributeSimilarityMaps = new TreeMap<String, AttributeSimilarityMap>();
		File dir = new File(fileName);
		for (File file : dir.listFiles()) {
			List<String> lines = FileUtils.readTextFromFileToLines(file);
			int idx = 0;
			for (String line : lines) {
				String[] parts = line.split(",");
				String attributeCode = parts[0];
				if (idx > 0) {
					AttributeSimilarityMap attributeattributeSimilarityMap = initAttributeSimilarityMap(parts);
					attributeSimilarityMaps.put(attributeCode, attributeattributeSimilarityMap);
				} else {
					log.info("Processing Attribute " + attributeCode + " size: " + lines.size());
				}
				idx++;
			}
		}
	}

	private AttributeSimilarityMap initAttributeSimilarityMap(String[] parts) {
		String attributeCode = parts[0];
		Entity value1Entity = new Entity(parts[1], "meta");
		Entity value2Entity = new Entity(parts[2], "meta");
		Double freq = Double.valueOf(parts[3]);
		initSingleValueAttributes(attributeCode, freq);

		AttributeSimilarityMap attributeattributeSimilarityMap = attributeSimilarityMaps.get(attributeCode);
		if (attributeattributeSimilarityMap == null) {
			attributeattributeSimilarityMap = new AttributeSimilarityMap();
		}
		@SuppressWarnings("unchecked")
		Map<Entity, Double> map = (Map<Entity, Double>) attributeattributeSimilarityMap.get(value1Entity);
		Map<Entity, Double> similarityMap = map;
		if (similarityMap == null) {
			similarityMap = new TreeMap<Entity, Double>();
		}
		if (RecommenderProperties.MIN_CUSTOM_ENTITY_SIMILARITY > 0
				&& freq >= RecommenderProperties.MIN_CUSTOM_ENTITY_SIMILARITY) {
			similarityMap.put(value2Entity, freq);
		}
		attributeattributeSimilarityMap.put(value1Entity,
				CollectionUtils.sortByValueDesc(similarityMap, MAX_NUMBER_OF_SIMILAR_ENTITIES));
		return attributeattributeSimilarityMap;
	}

	private void initSingleValueAttributes(String attributeCode, Double freq) {
		singleValueAttributes.add(attributeCode);
		if (freq < 1.0) {
			singleValueAttributes.remove(attributeCode);
		}
	}

	public Map<String, AttributeSimilarityMap> getAttributeSimilarityMaps() {
		return attributeSimilarityMaps;
	}

	public AttributeSimilarityMap getAttributeSimilarityMap(String attributeCode) {
		return attributeSimilarityMaps.get(attributeCode);
	}

	public Set<String> getSingleValueAttributes() {
		return singleValueAttributes;
	}

	public static void setMAX_NUMBER_OF_SIMILAR_ENTITIES(int mAX_NUMBER_OF_SIMILAR_ENTITIES) {
		MAX_NUMBER_OF_SIMILAR_ENTITIES = mAX_NUMBER_OF_SIMILAR_ENTITIES;
	}

}
