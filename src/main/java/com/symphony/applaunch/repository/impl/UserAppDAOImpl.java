package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.AppType;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.repository.GenericDAO;
import com.symphony.applaunch.repository.IUserAppDAO;
import com.symphony.applaunch.repository.IUserDAO;
import com.symphony.applaunch.util.DateHelper;
import jakarta.persistence.criteria.*;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Transactional
@Repository("userAppDAOImpl")
public class UserAppDAOImpl extends GenericDAO<UserApp, Long> implements IUserAppDAO {

    private IUserDAO userDAO;

    public UserAppDAOImpl(IUserDAO userDAO) {
        super(UserApp.class);
        this.userDAO = userDAO;
    }
    private static final Logger logger = LoggerFactory.getLogger(UserAppDAOImpl.class);

    /**
     * This method is used to find user
     *
     * @param user object and displayType
     * @return ResponseEntity list user apps subscribed to that user
     * @throws ApplicationException throws in case of query or DB Error.
     *
     */
    @Override
    public List<UserApp> findByUserId(Users user, String displayType) {
        logger.info("UserAppDAOImpl :: findByUserId() called!");
        try {
            CriteriaBuilder criteriaBuilder = getCrntSession().getCriteriaBuilder();
            CriteriaQuery<UserApp> criteriaQuery = criteriaBuilder.createQuery(UserApp.class);
            Root<UserApp> root = criteriaQuery.from(UserApp.class);
            Join<UserApp, SHSApp> shsAppJoin = root.join("shsApp", JoinType.LEFT);
            Join<SHSApp, AppType> appTypeJoin = shsAppJoin.join("appType", JoinType.LEFT);

            Predicate userIdPredicate = criteriaBuilder.equal(root.get("userId"), user.getId());
            Predicate marketIdIsNullPredicate = criteriaBuilder.isNull(root.get("marketId"));
            Predicate startDatePredicate = criteriaBuilder.or(
                    criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), DateHelper.getCurrentTime()),
                    criteriaBuilder.isNull(root.get("startDate")));
            Predicate expirationDatePredicate = criteriaBuilder.or(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), DateHelper.getCurrentTime()),
                    criteriaBuilder.isNull(root.get("expirationDate")));
            Predicate displayTypePredicate;
            String displayTypeString = "displayType";
            String isInternal = "isInternal";
            if (displayType != null) {
                displayTypePredicate = criteriaBuilder.equal(shsAppJoin.get(displayTypeString), displayType);
            } else {
                displayTypePredicate = criteriaBuilder.or(criteriaBuilder.equal(shsAppJoin.get(displayTypeString), "AP"),
                        criteriaBuilder.isNull(shsAppJoin.get(displayTypeString)));
            }
            Predicate isInternalPredicate;
            if (user.getIsInternal() != null && Boolean.FALSE.equals(user.getIsInternal())) {
                isInternalPredicate = criteriaBuilder.or(criteriaBuilder.equal(appTypeJoin.get(isInternal), false),
                        criteriaBuilder.isNull(appTypeJoin.get(isInternal)));
            } else {
                isInternalPredicate = criteriaBuilder.or(criteriaBuilder.equal(appTypeJoin.get(isInternal), true),
                        criteriaBuilder.isNull(appTypeJoin.get(isInternal)));
            }

            criteriaQuery.select(root).where(userIdPredicate, marketIdIsNullPredicate, startDatePredicate,
                    expirationDatePredicate, displayTypePredicate, isInternalPredicate);

            return getCrntSession().createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
        return Collections.emptyList();
    }

    /**
     * This method is used to find user
     *
     * @param user object
     * @return ResponseEntity list user apps subscribed to that user
     * @throws ApplicationException throws in case of query or DB Error.
     *
     */
    @Override
    public List<UserApp> findByUserId(Users user) {
        logger.info("UserAppDAOImpl :: findByUserId() with only one param-User called!");
        return findByUserId(user, null);
    }

    @Override
    public UserApp getByUserAndApp(Long userId, SHSApp shsApp) {
        logger.info("userAppDaoImpl :: getByUserAndApp() called!");
        StringBuilder userAppsData = new StringBuilder();
        Users user = userDAO.findOne(userId);
        userAppsData.append(ApplicationConstants.USER_APP_DATA_APPEND_TABLE);
        userAppsData.append(" FROM UserApp uap where  uap.userId=" + userId + " ");
        userAppsData.append(" and  uap.shsApp.id=" + shsApp.getId() + " ");
        if (user.getIsInternal() != null && user.getIsInternal())
            logger.info(ApplicationConstants.ALL_APPS);
        else
            userAppsData.append(
                    ApplicationConstants.UAP_DOT_SHSAPP_DOT_APPTYPE_ISINTERNAL_IS_FALSE_OR_NULL_AND_UAP_DOT_SHSAPP_ISINTERNAL_IS_FALSE_OR_NULL);//

        userAppsData.append(
                ApplicationConstants.UAP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_AND_UAP_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE);
        userAppsData.append(" and  (uap.shsApp.displayType='AP' OR uap.shsApp.displayType='MDM') ");

        userAppsData.append(
                ApplicationConstants.UAP_DOT_SHSAPP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_STARTDATE_AND_UAP_DOT_SHSAPP_DOT_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE);
        userAppsData.append(ApplicationConstants.ORDER_BY_UAP_DOT_SHSAPP_DOT_NAME);

        Query query = getCrntSession().createQuery(userAppsData.toString());/* NOSONAR */
        query.setParameter(ApplicationConstants.START_DATE, new Date());
        query.setResultTransformer(Transformers.aliasToBean(UserApp.class));
        List<UserApp> userApps1 = executeHQLSelectQuery(query);

        List<UserApp> userAppsTemp = new ArrayList<>();

        userAppsData.setLength(0);
        userAppsData.append(ApplicationConstants.USER_APP_DATA_APPEND_TABLE);
        userAppsData.append(" FROM UserApp uap, Users u, MarketUser mu where  (u.id =" + userId
                + " and ((uap.userId is null and uap.companyId = u.company.id and uap.marketId is null) or (uap.userId is null and uap.companyId = u.company.id and uap.marketId = mu.market.id and mu.market.isActive is true and mu.user.id ="
                + userId + "))) ");
        userAppsData.append(" and  uap.shsApp.id=" + shsApp.getId() + " ");
        if (user.getIsInternal() != null && user.getIsInternal())
            logger.info(ApplicationConstants.ALL_APPS);
        else
            userAppsData.append(
                    ApplicationConstants.UAP_DOT_SHSAPP_DOT_APPTYPE_ISINTERNAL_IS_FALSE_OR_NULL_AND_UAP_DOT_SHSAPP_ISINTERNAL_IS_FALSE_OR_NULL);//

        userAppsData.append(" and uap.shsApp.displayType='AP' ");

        userAppsData.append(
                ApplicationConstants.UAP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_AND_UAP_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE);
        userAppsData.append(
                ApplicationConstants.UAP_DOT_SHSAPP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_STARTDATE_AND_UAP_DOT_SHSAPP_DOT_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE);
        userAppsData.append(ApplicationConstants.ORDER_BY_UAP_DOT_SHSAPP_DOT_NAME);

        Query query1 = getCrntSession().createQuery(userAppsData.toString());/* NOSONAR */
        query1.setParameter(ApplicationConstants.START_DATE, new Date());
        query1.setResultTransformer(Transformers.aliasToBean(UserApp.class));/* NOSONAR */
        List<UserApp> userApps = executeHQLSelectQuery(query1);
        UserApp userAppExist = null;

        boolean isFlag = false;
        if (!userApps.isEmpty()) {
            if (!userApps1.isEmpty()) {
                for (UserApp appType1 : userApps1) {
                    for (int i = 0; i < userApps.size(); i++) {
                        if (userApps.get(i).getShsApp().getId().equals(appType1.getShsApp().getId())) {
                            isFlag = true;
                            break;
                        }
                    }
                    if (!isFlag)
                        userAppsTemp.add(appType1);
                    isFlag = false;
                }
            }
        } else {
            userApps.addAll(userApps1);
        }
        if (!userAppsTemp.isEmpty())
            userApps.addAll(userAppsTemp);

        if (!userApps.isEmpty()) {
            userAppExist = userApps.get(0);
        }

        return userAppExist;

    }

}
