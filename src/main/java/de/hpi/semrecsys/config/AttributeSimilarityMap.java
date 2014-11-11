package de.hpi.semrecsys.config;

import java.util.Map;
import java.util.TreeMap;

import de.hpi.semrecsys.model.Entity;

/**
 * Container for attribute similarity
 * Entity -> Entity, Double
 * @author Michael Wolowyk
 *
 */
public class AttributeSimilarityMap extends TreeMap<Entity, Map<? extends Entity, Double>> {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (java.util.Map.Entry<Entity, Map<? extends Entity, Double>> entry : this.entrySet()) {
			builder.append(entry.getKey()).append("\n");
			for (java.util.Map.Entry<? extends Entity, Double> entry2 : entry.getValue().entrySet()) {
				builder.append("\t").append(entry2.getKey()).append(" : ").append(entry2.getValue()).append("\n");
			}
		}
		return builder.toString();
	}

}