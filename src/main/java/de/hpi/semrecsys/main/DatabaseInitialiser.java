package de.hpi.semrecsys.main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.config.SemRecSysConfigurator.Customer;
import de.hpi.semrecsys.model.Attribute.AttributeType;
import de.hpi.semrecsys.persistence.BasicDAO;
import de.hpi.semrecsys.utils.CollectionUtils;
import de.hpi.semrecsys.utils.FileUtils;


/***
 * Used for execution of the initial ETL Script to bring the database to 
 * a good fitting data format for the analysis
 * @author Michael Wolowyk
 *
 */
public class DatabaseInitialiser {

	private static Customer customer = Customer.melovely;

	private static boolean cleanDatabase = true;
	private static String targetSchema;
	private static boolean dbInitMode = true;

	private static String cleanDbPath = "sql/clean_db.sql";
	private static String initDbPath = customer.name() + "/init_db.sql";

	private static SemRecSysConfigurator configurator = SemRecSysConfigurator.getDefaultConfigurator(customer,
			dbInitMode);
	private static String structuredAttributes;
	private static String unstructuredAttributes;
	private static String splitAttributes;

	private static CharSequence imgBasePath;

	public static void execute() {

		String customerWebpage = configurator.getJsonProperties().getCustomerWebsite();
		imgBasePath = customerWebpage + "media/catalog/product";

		targetSchema = configurator.getTargetSchema();
		structuredAttributes = getAttributesStringByType(AttributeType.struct);
		unstructuredAttributes = getAttributesStringByType(AttributeType.unstruct);
		splitAttributes = getAttributesStringByType(AttributeType.split);
		initDatabase();
		cleanDatabase();
	}

	public static void main(String[] args) throws IOException, SQLException {
		execute();
	}

	private static void initDatabase() {
		String sqlText = getInitScript();
		BasicDAO manager = BasicDAO.getDefault();
		manager.executeSQLUpdate(sqlText);
	}

	private static void cleanDatabase() {
		BasicDAO manager = BasicDAO.getDefault();
		String sqlText = getCleanScript();
		if (cleanDatabase) {
			manager.executeSQLUpdate(sqlText);
		}
	}

	private static String getInitScript() {
		System.out.println("Fill database for " + configurator.getJsonProperties().getCustomer());

		System.out.println("found attributes: \nstructured: " + structuredAttributes + "\nunstructured: "
				+ unstructuredAttributes + "\nsplitted: " + splitAttributes);
		String scriptPath = SemRecSysConfigurator.getPropertiesDirPath() + initDbPath;
		String sqlText = FileUtils.readTextFromFile(scriptPath);

		sqlText = sqlText.replace("$SCHEMA_NAME$", targetSchema).replace("$BASE_SCHEMA$", configurator.getSrcSchema())
				.replace("$STRUCT_ATTRIBUTES$", structuredAttributes)
				.replace("$UNSTRUCT_ATTRIBUTES$", unstructuredAttributes)
				.replace("$SPLIT_ATTRIBUTES$", splitAttributes).replace("$IMG_BASE$", imgBasePath);
		return sqlText;
	}

	private static String getAttributesStringByType(AttributeType type) {
		return CollectionUtils.collectionToString(getAttributes(type));
	}

	private static Set<String> getAttributes(AttributeType type) {
		return configurator.getJsonProperties().getAttributesByType(type).keySet();
	}

	private static String getCleanScript() {
		String scriptPath = SemRecSysConfigurator.getPropertiesDirPath() + cleanDbPath;
		String sqlText = FileUtils.readTextFromFile(scriptPath);
		sqlText = sqlText.replace("$SCHEMA_NAME$", targetSchema);
		return sqlText;
	}

}
