package de.hpi.semrecsys.config;

import java.io.File;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import de.hpi.semrecsys.utils.FileUtils;

/**
 * Configurates Hibernate
 * @author Michael Wolowyk
 *
 */
public class HibernateConfigurator {
	private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;

	private static String HIBERNATE_CFG_LOCATION = null;
	private static String DB_PROPERTIES_LOCATION = null;
	private static String DB_URL_PARAM_NAME = "jdbc.url";
	private static String DB_DRIVER_PARAM_NAME = "jdbc.driverClassName";
	private static String DB_USER_PARAM_NAME = "jdbc.username";
	private static String DB_PASSWORD_NAME = "jdbc.password";
	public static String DB_TARGET_SCHEMA = "jdbc.target_schema";
	public static String DB_SRC_SCHEMA = "jdbc.src_schema";

	static {

		try {
			DB_PROPERTIES_LOCATION = SemRecSysConfigurator.getCustomerPropertiesPath() + "/database.properties";
			Properties databaseProperties = FileUtils.readProperties(DB_PROPERTIES_LOCATION);
            HIBERNATE_CFG_LOCATION = SemRecSysConfigurator.getCustomerPropertiesPath() + "/hibernate.cfg.xml";

			String dburl = databaseProperties.getProperty(DB_URL_PARAM_NAME);
			if (!dburl.endsWith("/")) {
				dburl += "/";
			}
			dburl += databaseProperties.getProperty(DB_TARGET_SCHEMA);

            Configuration configuration = new Configuration().configure(new File(HIBERNATE_CFG_LOCATION));
			configuration.setProperty(Environment.URL, dburl);

			configuration.setProperty(Environment.DRIVER, databaseProperties.getProperty(DB_DRIVER_PARAM_NAME));
			configuration.setProperty(Environment.USER, databaseProperties.getProperty(DB_USER_PARAM_NAME));
			configuration.setProperty(Environment.PASS, databaseProperties.getProperty(DB_PASSWORD_NAME));
			configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
			configuration.setProperty(AvailableSettings.TRANSACTION_STRATEGY, JdbcTransactionFactory.class.getName());
			configuration.getProperties().remove(AvailableSettings.DEFAULT_SCHEMA);
			serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties())
					.buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (HibernateException he) {
			System.err.println("Error creating Session: " + he);
			throw new ExceptionInInitializerError(he);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
