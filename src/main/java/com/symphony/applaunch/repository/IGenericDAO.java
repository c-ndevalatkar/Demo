package com.symphony.applaunch.repository;

import com.symphony.applaunch.exception.ApplicationException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({ "rawtypes" })
public interface IGenericDAO<E, PK extends Serializable> {

    /**
     * This method is used to save entity into the database
     *
     * @param newInstance Represents the instance to be saved.
     *
     * @return PK Represents the primary key of the entity
     * @throws ApplicationException throws in case of query or DB Error.
     */
    PK save(E newInstance);

    /**
     * This method is used to update the entity into database.
     *
     * @param entity Represents the instance to be updated.
     *
     * @see org.hibernate.Session#saveOrUpdate(java.lang.Object)
     * @throws ApplicationException throws in case of query or DB Error.
     */
    void update(E entity);

    /**
     * This method is used to save object into the database
     *
     * @param object object
     * @return id
     * @throws ApplicationException throws in case of query or DB Error.
     */
    public PK saveEntity(Object object);

    /**
     * This method is used to update object into the database
     *
     * @param newInstance newInstance
     *
     * @throws ApplicationException throws in case of query or DB Error.
     */
    public void updateEntity(Object newInstance);

    /**
     * This method is used to save Or update all objects into the database
     *
     * @param objects represents collection of objects
     *
     * @throws ApplicationException throws in case of query or DB Error.
     */
    public void saveOrUpdateAll(Collection<?> objects);

    /**
     * This method is used to get the particular row from database and its columns
     * will mapped to the object describes in the class signature E.
     *
     * @param id Represents the primary key value for which objects needs to be
     *           fetched.
     *
     * @return entity Represents the mapped object from database row.
     *
     * @see org.hibernate.Session#get(java.lang.Class, java.io.Serializable)
     * @throws ApplicationException throws in case of query or DB Error.
     */
    E get(PK id);

    /**
     * This method is used to load the proxy for the object describes in the class
     * signature E. This method never hit the database until the property (except id
     * property) of that object() is accessed by getter.
     *
     * @param id Represents the id of the object for which the proxy needs to be
     *           load.
     *
     * @return entity Represents the proxy object.
     *
     * @see org.hibernate.Session#load(java.lang.Class, java.io.Serializable)
     * @throws ApplicationException throws in case of query or DB Error.
     */
    E load(PK id);

    /**
     * This method is used to load the proxy for the generic object.This method
     * never hit the database until the property (except id property) of that
     * object() is accessed by getter.
     *
     * @param entityClass Represents the class for which the proxy needs to be
     *                    generated.
     * @param id          Represents the id for which proxy needs to be generated.
     *
     * @return V Represents the proxy object.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    <V extends Object> V loadObject(Class<V> entityClass, PK id);

    /**
     * This method is used to load the proxy for the generic object.This method
     * never hit the database until the property (except id property) of that
     * object() is accessed by getter.
     *
     * @param classs Represents the class for which the proxy needs to be generated.
     * @param id     Represents the id for which proxy needs to be generated.
     *
     * @return V Represents the proxy object.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    <V extends Object> V loadMasterObject(Class<V> classs, Integer id);

    /**
     * This method is used to load the generic object from the database.
     *
     * @param entityClass Represents the class for which the object needs to be fetched
     *               from database.
     * @param id     Represents the id for which the object needs to be fetched from
     *               database.
     *
     * @return V Represents the generic object.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    <V extends Object> V getObject(Class<V> entityClass, PK id);

    /**
     * This method is used to get the generic object from the database.
     *
     * @param classs Represents the class for which the object needs to be fetched
     *               from database.
     * @param id     Represents the id for which the object needs to be fetched from
     *               database.
     *
     * @return V Represents the generic object.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    <V extends Object> V getMasterObject(Class<V> entityClass, Integer id);

    /**
     * This method is used to get Query object for Hibernate HQL Query.
     *
     * @param hql Represents the HQL for which the Query object needs to be fetched.
     *
     * @return Query<E> Represents the Query object.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    Query<E> getQueryObject(String hql);

    /**
     * This method is used to get Query object for named Query in hibernate.
     *
     * @param namedQuery
     *
     * @return Query<E> Represents the Query object.
     * @throws ApplicationException throws in case of query or DB Error
     */
    Query<E> getQueryFromNamedQuery(String namedQuery);

    /**
     * This method is used to get the SQLQuery object for sql query
     *
     * @param sql
     * @return NativeQuery<E>
     * @throws ApplicationException throws in case of query or DB Error.
     */
    NativeQuery<E> getSQLQueryObj(String sql);

    /**
     * This method is used to get the results from the Hibernate DDL Query object.
     *
     * @param query Represents the hibernate query object for which the results
     *              needs to be fetched.
     *
     * @return List Represents the result returns from the database.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    <V> List<V> executeHQLSelectQuery(Query<V> query);

    /**
     * @param entity
     * @throws ApplicationException
     */
    void delete(E entity);

    public <V> V executeUniqueResultHqlQuery(Query<V> query);

    List<E> getAll();

    public void saveOrUpdate(E entity);

    void saveOrUpdateEntity(Object newInstance);

    void clearSession();

    E mergeSession(E entity);

}
