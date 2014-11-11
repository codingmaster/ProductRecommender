package de.hpi.semrecsys.config;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.persistence.ProductDAO;

/**
 * Java Code to get a color name from rgb/hex value/awt color
 * 
 * The part of looking up a color name from the rgb values is edited from
 * https://gist.github.com/nightlark/6482130#file-gistfile1-java (that has some
 * errors) by Ryan Mast (nightlark)
 * 
 * @author Xiaoxiao Li
 * 
 */
public class ColorUtils {

	private Map<String, ColorEntity> colors;
	Logger log = Logger.getLogger(getClass().getName());
	private List<String> databaseColors;

	public ColorUtils(Map<String, ColorEntity> colors) {
		this.databaseColors = ProductDAO.getDefault().getAllColors();
		this.colors = colors;
	}

	public ColorUtils(SemRecSysConfigurator configurator) {
		this.databaseColors = ProductDAO.getDefault().getAllColors();
		this.colors = configurator.getColors();
	}

	public ColorSimilarity findSimilarColors(ColorEntity color) {
		return findSimilarColors(color, -1);
	}

	public AttributeSimilarityMap getColorSimilarityMap(Double threshold) {
		AttributeSimilarityMap attributeSimilarityMap = new AttributeSimilarityMap();
		for (String colorName : databaseColors) {
			Entity colorKey = new Entity(colorName, "meta");
			ColorSimilarity similarColors = findSimilarColors(colorName, threshold);
			if (similarColors != null) {
				attributeSimilarityMap.put(colorKey, similarColors.getColorSimilarityMap());
			}
		}
		return attributeSimilarityMap;
	}

	public String toString(double threshold) {
		Map<Entity, Map<? extends Entity, Double>> colorSimilarityMap = getColorSimilarityMap(threshold);
		return toString(colorSimilarityMap);
	}

	public String toString(Map<Entity, Map<? extends Entity, Double>> colorSimilarityMap) {
		StringBuilder builder = new StringBuilder();
		for (Entry<Entity, Map<? extends Entity, Double>> colorSimilarityEntry : colorSimilarityMap.entrySet()) {
			String keyColor = colorSimilarityEntry.getKey().getName();
			String header = "color_primary," + keyColor + ",";
			for (Entity valueEntity : colorSimilarityEntry.getValue().keySet()) {
				Double simValue = colorSimilarityEntry.getValue().get(valueEntity);
				String value = valueEntity.getName();
				builder.append(header).append(value).append(",").append(simValue).append("\n");
			}

		}

		return builder.toString();
	}

	/**
	 * Search for similar colors to a colorName with maximal threshold
	 * @param colorName
	 * @param threshold
	 * @return colorSimilarity map
	 */
	public ColorSimilarity findSimilarColors(String colorName, double threshold) {
		ColorEntity color = colors.get(colorName);
		if (color != null) {
			return findSimilarColors(color, threshold);
		} else {
			log.warning("Color: " + colorName + " wasnt found in color list. ");
		}
		return null;
	}

	/**
	 * Search for similar colors to a baseColor with maximal threshold
	 * @param baseColor
	 * @param threshold
	 * @return colorSimilarity map
	 */
	public ColorSimilarity findSimilarColors(ColorEntity baseColor, double threshold) {
		ColorSimilarity colorSimilarity = new ColorSimilarity(baseColor);
		colorSimilarity.addSimilarColor(baseColor, 1.0);
		double mseSimilarity;
		for (ColorEntity c : colors.values()) {
			mseSimilarity = c.computeMSESimilarity(baseColor);
			String name = c.getName();
			if (databaseColors != null && databaseColors.contains(name)) {
				if (!baseColor.equals(c) && threshold > 0 && mseSimilarity >= threshold) {
					colorSimilarity.addSimilarColor(c, mseSimilarity);
				}
			}
		}
		return colorSimilarity;
	}

	/**
	 * Get the closest color name from our list
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public ColorEntity getColorNameFromRgb(int r, int g, int b) {
		ColorEntity closestMatch = null;
		double minMSE = Double.MAX_VALUE;
		double mse;
		for (ColorEntity c : colors.values()) {
			mse = c.computeMSE(r, g, b);
			if (mse < minMSE) {
				minMSE = mse;
				closestMatch = c;
			}
		}

		return closestMatch;

	}

	/**
	 * Convert hexColor to rgb, then call getColorNameFromRgb(r, g, b)
	 * 
	 * @param hexColor
	 * @return
	 */
	public ColorEntity getColorNameFromHex(int hexColor) {
		int r = (hexColor & 0xFF0000) >> 16;
		int g = (hexColor & 0xFF00) >> 8;
		int b = (hexColor & 0xFF);
		return getColorNameFromRgb(r, g, b);
	}

	public int colorToHex(Color c) {
		return Integer.decode("0x" + Integer.toHexString(c.getRGB()).substring(2));
	}

	public ColorEntity getColorNameFromColor(Color color) {
		return getColorNameFromRgb(color.getRed(), color.getGreen(), color.getBlue());
	}

	public ColorEntity getColor(String name) {
		name = name.trim().toLowerCase();
		ColorEntity color = colors.get(name);
		if (color == null) {
			for (ColorEntity tmpColor : colors.values()) {
				for (String colorName : tmpColor.getColorNames().values()) {
					if (colorName.equalsIgnoreCase(name)) {
						return tmpColor;
					}
				}
			}
		}
		return color;
	}

	public Map<String, ColorEntity> getColors() {
		return colors;
	}

}