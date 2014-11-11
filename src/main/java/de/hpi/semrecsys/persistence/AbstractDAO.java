package de.hpi.semrecsys.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import de.hpi.semrecsys.DBObject;
import de.hpi.semrecsys.config.HibernateConfigurator;

/***
 * abstract handler for persistence objects uses {@link de.hpi.semrecsys.DBObject} <br>
 *
 * handles {@link org.hibernate.SessionFactory}<br>
 *
 * implements basic functionality common for all<br>
 *
 * represents standard data access object pattern<br>
 * @see <a href="http://en.wikipedia.org/wiki/Data_access_object">DAO</a>
 */
public abstract class AbstractDAO {

	protected static final int BATCH_SIZE = 40;
	protected final Log log = LogFactory.getLog(getClass());
	protected final SessionFactory sessionFactory = getSessionFactory();

	protected abstract Class<?> getType();

	protected SessionFactory getSessionFactory() {
		return HibernateConfigurator.getSessionFactory();
	}


	protected Session getSession() {
		commitCurrentTransaction();
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		return session;
	}

	private void commitCurrentTransaction() {
		if (sessionFactory.getCurrentSession().getTransaction().isActive()) {
			sessionFactory.getCurrentSession().getTransaction().commit();
		}
	}

	protected void commitTransaction(Session session) {
		session.getTransaction().commit();
	}

    /**
     * persist {@link de.hpi.semrecsys.DBObject} instance
     * @param instance
     * @return persisted object
     */
	public DBObject persist(DBObject instance) {
		log.trace("persisting " + getType() + " instance");
		Session session = getSession();
		try {
			session.persist(instance);
			log.trace("persist successful");
		} catch (RuntimeException re) {
			throw re;
		}
		commitTransaction(session);
		return instance;
	}

    /**
     * save or update {@link de.hpi.semrecsys.DBObject} instance
     * @param instance
     */
	public void attachDirty(DBObject instance) {
		Session session = getSession();
		log.trace("attaching dirty " + getType() + " instance");
		try {
			session.saveOrUpdate(instance);
			log.trace("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
		commitTransaction(session);
	}

    /**
     * delete {@link de.hpi.semrecsys.DBObject} instance
     * @param instance
     */
	public void delete(DBObject instance) {
		log.trace("deleting " + getType() + " instance");

		Session session = getSession();
		if (instance != null) {
			session.clear();
			try {
				sessionFactory.getCurrentSession().delete(instance);
				log.trace("delete successful");
			} catch (RuntimeException re) {
				log.error("delete failed for: " + instance, re);
				throw re;
			}
			commitTransaction(session);
		}
	}

    /**
     * delete all instances {@link de.hpi.semrecsys.DBObject}
     * @param instances
     */
	public void deleteAll(List<DBObject> instances) {
		for (DBObject obj : instances) {
			delete(obj);
		}
	}

    /**
     * find all elements
     * @return found objects
     */
	@SuppressWarnings("unchecked")
	public List<Object> findAll() {
		Session session = getSession();
		return session.createCriteria(getType()).list();
	}

    /**
     * find object by id
     * @param id
     * @return object
     */
	public Object findById(Serializable id) {
		log.trace("getting " + getType() + " instance with id: " + id);
		Session session = getSession();
		try {
			Object instance = session.get(getType(), id);
			if (instance == null) {
				log.trace("get successful, no instance found");
			} else {
				log.trace("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

    /**
     * find objects by example instance
     * @param instance
     * @return list of objects
     */
	@SuppressWarnings("unchecked")
	public List<DBObject> findByExample(DBObject instance) {
		log.trace("finding " + getType() + " instance by example");
		List<DBObject> results = new ArrayList<DBObject>();
		Session session = getSession();
		try {
			Criteria criteria = session.createCriteria(getType()).add(Example.create(instance));

			results = criteria.list();
			log.trace("find by example successful, result size: " + results.size());
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
		return results;
	}

    /**
     * checks whether object exists in the database
     * @param object
     * @return true if exists, false otherwise
     */
	public boolean isEntityExists(DBObject object) {
		Object findById = findById(object.getId());
		return findById != null;
	}

    /**
     * get number of objects in the database
     * @return number of objects
     */
	public int getSize() {
		Session session = getSession();
		String hql = "select count(*) from " + getType().getSimpleName();
		Integer size = ((Long) session.createQuery(hql).uniqueResult()).intValue();
		return size;

	}

}
