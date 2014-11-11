package de.hpi.semrecsys;

import java.io.Serializable;

/**
 * Interface used for common representation of database objects
 * @author Michael Wolowyk
 *
 */
public interface DBObject {
	public abstract Serializable getId();
}
