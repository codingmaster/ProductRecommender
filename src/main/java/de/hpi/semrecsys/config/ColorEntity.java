package de.hpi.semrecsys.config;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;
import de.hpi.semrecsys.model.Entity;

/**
 * Entity used for representation of colors
 * @author Michael Wolowyk
 *
 */
public class ColorEntity extends Entity {

	private int r, g, b;
	private float h, s, v;
	private String hex;
	private Map<LanguageCode, String> colorNames = new HashMap<LanguageCode, String>();
	private String name;
	public static LanguageCode defaultLanguage = LanguageCode.DE;
	public static final double MAX_MSE = 255 * Math.sqrt(3);

	/**
	 * 
	 * @param name color name
	 * @param rgb color rgb in form of r,g,b
	 */
	public ColorEntity(String name, String rgb) {
		super(name.toLowerCase(), "meta");
		this.name = name.toLowerCase();

		String[] rgbParts = rgb.split(",");
		this.r = Integer.valueOf(rgbParts[0].trim());
		this.g = Integer.valueOf(rgbParts[1].trim());
		this.b = Integer.valueOf(rgbParts[2].trim());

		float[] hsv = this.toHsv();
		this.h = hsv[0];
		this.s = hsv[1];
		this.v = hsv[2];

		addColorName(defaultLanguage, this.name);

	}

	/**
	 * Calculates Mean Square Error color similarity between this and the colorObject
	 * @param colorObject
	 * @return Similarity as Mean Square Error
	 */
	public double computeMSESimilarity(ColorEntity colorObject) {
		double computedMSE = computeMSE(colorObject.getR(), colorObject.getG(), colorObject.getB());
		return (MAX_MSE - computedMSE) / MAX_MSE;
	}

	
	/**
	 * Calculates Mean Square Error color similarity between this and the object with r1,g1,b1
	 * @param r1 - red
	 * @param g1 - green
	 * @param b1 - blue
	 * @return Mean Square Error similarity
	 */
	public double computeMSE(float r1, float g1, float b1) {
		return Math.sqrt((r1 - r) * (r1 - r) + (g1 - g) * (g1 - g) + (b1 - b) * (b1 - b));
	}


	/**
	 * add color name in language
	 * @param language
	 * @param name
	 */
	public void addColorName(LanguageCode language, String name) {
		colorNames.put(language, name.toLowerCase());
	}

	public String getColorName(LanguageCode language) {
		return colorNames.get(language);
	}

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
	}

	public float getH() {
		return h;
	}

	public float getS() {
		return s;
	}

	public float getV() {
		return v;
	}

	public String getName() {
		return colorNames.get(defaultLanguage);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorEntity other = (ColorEntity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void setName(String name) {
		colorNames.put(defaultLanguage, name);

	}

	public String getHex() {
		return hex;
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	public Map<LanguageCode, String> getColorNames() {
		return colorNames;
	}

	/**
	 * Transform this color to HSV 
	 * @return HSV value
	 */
	public float[] toHsv() {
		return Color.RGBtoHSB(r, g, b, null);
	}

	@Override
	public String toString() {
		return getName() + " hsv(" + h + "," + s + "," + v + ")";
	}

}