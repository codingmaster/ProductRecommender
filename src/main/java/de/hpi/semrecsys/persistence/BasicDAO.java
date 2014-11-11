package de.hpi.semrecsys.persistence;

import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

/**
 * basic DAO functionality
 */
public class BasicDAO extends AbstractDAO {
	static BasicDAO instance;

	private BasicDAO() {

	}

	public static BasicDAO getDefault() {
		if (instance == null) {
			instance = new BasicDAO();
		}
		return instance;
	}

	@Override
	protected Class<Object> getType() {
		return Object.class;
	}

	public void executeSQLUpdate(final String sql) {
		Session session = getSession();
		try {
			session.doWork(new Work() {

				@Override
				public void execute(java.sql.Connection connection) throws SQLException {
					String[] scripts = sql.split(";");
					for (String script : scripts) {
						Statement st = connection.createStatement();
						script = script.trim();
						if (script != null && !script.isEmpty()) {
							log.info("Executing: " + script + "\n");
							st.executeUpdate(script);
						}
						st.close();
					}

				}
			});
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		commitTransaction(session);

	}

}
