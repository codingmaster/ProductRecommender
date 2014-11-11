package de.hpi.semrecsys.config;

import de.hpi.semrecsys.config.SemRecSysConfigurator.Customer;
import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;

/**
 * Container for configuration parameters 
 * @author Michael Wolowyk
 *
 */
public class ConfiguratorParameters {
	public Customer customer;
	public LanguageCode languageCode = LanguageCode.DE;
	public String propsDirPath;
	public boolean dbInitMode = false;

	public ConfiguratorParameters(Customer customer) {
		this.customer = customer;
	}

	public ConfiguratorParameters(Customer customer, LanguageCode languageCode, String propsDirPath) {
		this.customer = customer;
		this.languageCode = languageCode;
		this.propsDirPath = propsDirPath;
	}

	public ConfiguratorParameters(Customer customer, LanguageCode languageCode, String propsDirPath, boolean dbInitMode) {
		this.customer = customer;
		this.languageCode = languageCode;
		this.propsDirPath = propsDirPath;
		this.dbInitMode = dbInitMode;
	}

}