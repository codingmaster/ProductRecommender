package de.hpi.semrecsys.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.utils.CollectionUtils;

/**
 * Container for color similarity values
 * ColorEntity -> ColorEntity, Double
 * @author Michael Wolowyk
 *
 */
public class ColorSimilarity {

	Map<ColorEntity, Double> colorSimilarityMap = new HashMap<ColorEntity, Double>();
	List<Entity> colorSimilarityList;
	private ColorEntity baseColor;

	public ColorSimilarity(ColorEntity baseColor) {
		this.baseColor = baseColor;
	}

	public void addSimilarColor(ColorEntity color, Double similarity) {
		colorSimilarityMap.put(color, similarity);
	}

	public Map<ColorEntity, Double> getColorSimilarityMap() {
		return CollectionUtils.sortByValueDesc(colorSimilarityMap);
	}

	public List<Entity> getColorSimilarityList() {
		if (colorSimilarityList == null) {
			colorSimilarityList = new ArrayList<Entity>();
			for (ColorEntity color : getColorSimilarityMap().keySet()) {
				colorSimilarityList.add(color);
			}
		}
		return colorSimilarityList;
	}

	public ColorEntity getBaseColor() {
		return baseColor;
	}

	@Override
	public String toString() {
		return CollectionUtils.mapToString(getColorSimilarityMap());
	}
}
