package de.hpi.semrecsys.webservice;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

/**
 * Connects to SPARQL endpoint and executes queries
 */
public class SparqlEndpointConnector {

	static Log log = LogFactory.getLog(SparqlEndpointConnector.class);

	public static ResultSet executeQuery(String sparqlQueryString, String service) {
		ResultSet results = null;

		QueryExecution qexec = null;
		CustomQuery query = null;
		try {
			query = new CustomQuery(QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ));

			log.debug("Executing SPARQL Query: " + query.toString());
			qexec = QueryExecutionFactory.sparqlService(service, query);
			results = qexec.execSelect();
		} catch (Exception ex) {
			log.error("Error occures by execution of query \n" + query + "\n" + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return results;
	}

	public static class CustomQuery extends Query {

		private Query query;

		public CustomQuery(Query query) {
			this.query = query;
		}

		@Override
		public String toString() {
			String queryString = query.toString();
			try {
				queryString = java.net.URLDecoder.decode(queryString, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return queryString;
		}
	}

}
