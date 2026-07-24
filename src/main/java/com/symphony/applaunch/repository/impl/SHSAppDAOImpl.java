package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.repository.GenericDAO;
import com.symphony.applaunch.repository.ISHSAppDAO;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;

import java.util.List;

@Transactional
@Repository("sHSAppDAOImpl")
public class SHSAppDAOImpl extends GenericDAO<SHSApp, Long> implements ISHSAppDAO {

    private static final Logger logger = LoggerFactory.getLogger(SHSAppDAOImpl.class);
    public SHSAppDAOImpl() {
        super(SHSApp.class);
    }

    /**
     * This method is used to get listing of apps.
     *
     * @return Represents the lsit of shsapps.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    @Override
    public List<SHSApp> findAll() {
        return getAll();
    }

    /**
     * This method is used to get one perticular app.
     *
     * @param appId Represents id of app that is to be reutrned.
     * @return Object of app.
     * @throws ApplicationException throws in case of query or DB Error.
     */
    @Override
    public SHSApp findOne(int appId) {
        try {
            Query<SHSApp> findOne = getQueryFromNamedQuery("SHSApp.findOne");
            findOne.setParameter("id", appId);
            return executeUniqueResultHqlQuery(findOne);
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
        return null;
    }
}
