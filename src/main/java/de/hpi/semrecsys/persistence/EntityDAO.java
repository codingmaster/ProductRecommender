package de.hpi.semrecsys.persistence;

import com.hp.hpl.jena.query.ResultSet;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.utils.Namespacer;
import de.hpi.semrecsys.webservice.SparqlEndpointConnector;

/**
 *
 */
public class EntityDAO {

    private SemRecSysConfigurator configurator;
    private Namespacer namespacer;

    public EntityDAO(SemRecSysConfigurator configurator) {
        this.configurator = configurator;
        this.namespacer = configurator.getNamespacer();
    }

    public static String getLongUri(Namespacer namespacer, Entity entity) {
        return namespacer.restore(entity.getUri());
    }



    public Integer getNumberOfConnections(Entity entity1, Entity entity2, int depth) {
        String query;
        ResultSet resultSet;
        Integer numberOfConnections = 0;
        query = getInterconnectionQuery(entity1, entity2, depth);
        resultSet = SparqlEndpointConnector.executeQuery(query, configurator.getDbpediaSparqlEndpoint());
        if (resultSet.hasNext()) {
            numberOfConnections = (Integer) resultSet.next().get("?count").asNode().getLiteralValue();
        }
        return numberOfConnections;
    }

    private String getInterconnectionQuery(Entity entity1, Entity entity2, int depth) {
        String result = "";
        String entity1Uri = "<" + entity1.getLongUri(namespacer) + ">";
        String entity2Uri = "<" + entity2.getLongUri(namespacer) + ">";
        if (depth == 0) {
            result = "SELECT (count(?prop) as ?count) WHERE { " + entity1Uri + " ?prop " + entity2Uri + ".}";
        } else if (depth == 1) {
            result = "SELECT (count(*) as ?count) WHERE { " + entity1Uri + " ?prop ?entity2. " + "?entity2 ?prop2 "
                    + entity2Uri + ". FILTER(?entity2 != " + entity2Uri + ")}";
        } else if (depth == 2) {
            result = "SELECT (count(*) as ?count) WHERE { " + entity1Uri + "?prop ?entity2."
                    + " ?entity2 ?prop2 ?entity3. " + "?entity3 ?prop3 " + entity2Uri + "."
                    + "FILTER(?entity2 != ?entity3 && ?entity3 != " + entity2Uri + "" + " && " + entity1Uri
                    + " != ?entity2)" + "}";
        }

        return result;
    }




}
