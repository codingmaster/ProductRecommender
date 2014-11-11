package de.hpi.semrecsys.model;

/**
 * Entities which could not be found in DBpedia and were created as customer specific
 * @author Michael Wolowyk
 *
 */
public class CustomEntity extends Entity {

	public CustomEntity(String uri) {
		super(uri, "meta");
		super.setMeta(true);
	}
}
