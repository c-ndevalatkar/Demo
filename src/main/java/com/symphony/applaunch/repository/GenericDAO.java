package com.symphony.applaunch.repository;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional
public class GenericDAO<E, PK extends java.io.Serializable> implements IGenericDAO<E, PK> {

    @PersistenceContext
    EntityManager em ;

    private final Class<E> entityclass;
    private static final Logger logger = LoggerFactory.getLogger(GenericDAO.class);

    public GenericDAO(final Class<E> entityClass) {
        super();
        entityclass = entityClass;
    }

    protected Session getCrntSession() {
        try {
            return em.unwrap(Session.class);
        } catch (final HibernateException e) {
            throw new ApplicationException("getCrntSession: Failed to get thread session ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public PK save(final E newInstance) {
        PK id = null;
        final Session session = getCrntSession();
        try {
            logger.debug("save: Adding Object  - {}", newInstance);
            id = (PK) session.save(newInstance); /* NOSONAR */
            logger.debug("save: Insertion completed successfully for  - {}", newInstance);
        } catch (final HibernateException e) {
            throw new ApplicationException("Failed to insert  " + newInstance + ApplicationConstants.SUCCESSFULLY,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
        return id;
    }

    @Override
    public void update(final E entity) {
        try {
            logger.debug("update: entity - {}", entity);
            getCrntSession().clear();
            getCrntSession().update(entity); /* NOSONAR */
        } catch (HibernateException e) {
            throw new ApplicationException("update: Exception while update entity "+ entity,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    public PK saveEntity(Object newInstance) {
        PK id = null;
        final Session session = getCrntSession();
        try {
            logger.debug("saveEntity: Adding Object  - {}", newInstance);
            id = (PK) session.save(newInstance); /* NOSONAR */
            logger.debug("saveEntity: Insertion completed successfully for  - {}", newInstance);
        } catch (HibernateException e) {
            throw new ApplicationException("saveEntity: Failed to insert  " + newInstance + ApplicationConstants.SUCCESSFULLY,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
        return id;
    }

    public void updateEntity(Object newInstance) {
        try {
            logger.debug("updateEntity: onject - {}", newInstance);
            getCrntSession().update(newInstance);/* NOSONAR */
        } catch (HibernateException e) {
            throw new ApplicationException("updateEntity :Exception while update entity "+ newInstance,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public void saveOrUpdateAll(Collection<?> objects) {
        try {
            logger.debug("saveOrUpdateAll: objects.size : {}", objects.size());
            for (Object object : objects) {
                logger.debug("saveOrUpdateAll: object : {}", object);
                getCrntSession().saveOrUpdate(object);/* NOSONAR */
            }
        } catch (HibernateException e) {
            throw new ApplicationException("saveOrUpdateAll: Exception while save or update All "+ objects,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public E get(final PK id) {
        try {
            logger.debug("get : PK : {}", id);
            return getCrntSession().get(entityclass, id);
        } catch (final HibernateException e) {
            throw new ApplicationException("get: Exception while get entity ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public E load(final PK id) {
        try {
            logger.debug("load: id {}", id);
            return getCrntSession().load(entityclass, id);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("load: Exception while load entity ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public <V> V loadObject(final Class<V> classs, final PK id) {
        try {
            logger.debug("loadObject: classs: {}, {} : {}", classs, ApplicationConstants.PK, id);
            return (V) getCrntSession().load(classs, id);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("loadObject: Exception while load entity "+classs +","+ id,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public <V> V loadMasterObject(final Class<V> classs, Integer id) {
        try {
            logger.debug("loadMasterObject: classs: {}, {} : {}", classs, ApplicationConstants.PK, id);
            return (V) getCrntSession().load(classs, id);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("loadMasterObject: Exception while load master object "+ classs + ", " + id,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public <V> V getObject(final Class<V> classs, final PK id) {
        try {
            logger.debug("getObject: classs: {}, {} : {}", classs, ApplicationConstants.PK, id);
            return (V) getCrntSession().get(classs, id);
        } catch (final HibernateException e) {
            throw new ApplicationException("getObject: Exception while get object "+ classs + ApplicationConstants.PK + id,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public <V> V getMasterObject(final Class<V> classs, final Integer id) {
        try {
            logger.debug("getMasterObject: classs: {}, {} : {}", classs, ApplicationConstants.PK, id);
            return (V) getCrntSession().get(classs, id);
        } catch (final HibernateException e) {
            throw new ApplicationException("getMasterObject: Exception while get master object "+ classs + ApplicationConstants.PK + id,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public Query<E> getQueryObject(final String hql) {
        try {
            return getCrntSession().createQuery(hql, entityclass);
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while creating query object " + hql,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public Query<E> getQueryFromNamedQuery(String namedQuery) {
        try {
            return getCrntSession().createNamedQuery(namedQuery, entityclass);
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while executing named HQL query " + namedQuery,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public NativeQuery<E> getSQLQueryObj(final String sql) {
        try {
            return getCrntSession().createNativeQuery(sql, entityclass);
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while creating sql query object " + sql,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public <V> List<V> executeHQLSelectQuery(final Query<V> query) {
        try {
            logger.info("executeHQLSelectQuery : {}", query);
            return query.list();
        } catch (Exception e) {
            throw new ApplicationException("Exception while executing hq query  " + query,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }

    }

    @Override
    public <V> V executeUniqueResultHqlQuery(final Query<V> query) {
        try {
            return query.uniqueResult();
        } catch (final HibernateException e) {
            throw new ApplicationException("Result should be unique  " + query,
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public void delete(E entity) {
        try {
            getCrntSession().delete(entity);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while executing delete query ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public List<E> getAll() {
        try {
            logger.debug("getAll:  Object  - ");

            return executeHQLSelectQuery(getQueryObject("FROM " + entityclass.getSimpleName()));
        } catch (HibernateException e) {
            throw new ApplicationException("Exception while executing getAll query ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public void saveOrUpdate(E entity) {
        try {
            getCrntSession().saveOrUpdate(entity);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while save or update ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public void saveOrUpdateEntity(Object newInstance) {
        try {
            getCrntSession().saveOrUpdate(newInstance);/* NOSONAR */
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while save or update ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public void clearSession() {
        try {
            getCrntSession().flush();
            getCrntSession().clear();
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while clear session ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }

    @Override
    public E mergeSession(E entity) {
        try {
            return getCrntSession().merge(entity);
        } catch (final HibernateException e) {
            throw new ApplicationException("Exception while merging session ",
                    ErrorCode.BASE_DB_ERROR.getCodeId(), e);
        }
    }
}
