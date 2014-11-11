package de.hpi.semrecsys.virtuoso;

import java.util.List;

import com.hp.hpl.jena.graph.Triple;

public abstract class AbstractTriplesCreator {
	public abstract List<Triple> createTriples(int number);

}
