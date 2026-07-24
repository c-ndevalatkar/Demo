package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.PaginationVO;
import com.symphony.applaunch.entity.UserLogin;
import com.symphony.applaunch.repository.GenericDAO;
import com.symphony.applaunch.repository.IUserLoginDAO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Transactional
@Repository("userLoginDAOImpl")
public class UserLoginDAOImpl extends GenericDAO<UserLogin, Long> implements IUserLoginDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginDAOImpl.class);

    public UserLoginDAOImpl() {
        super(UserLogin.class);
    }

    @Override
    public Long saveUserLogin(UserLogin userLogin) {
        return save(userLogin);
    }

    /**
     * @return all user logins count
     */
    /* SHSP-CR-0005 */
    @Override
    public Integer getAllUserLoginsCount() {
        try {
            Query<Long> countQuery = getCrntSession().createNamedQuery("UserLogins.getAllUserLoginsCount", Long.class);
            Long count = executeUniqueResultHqlQuery(countQuery);
            return (count == null ? 0 : count.intValue());
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            return 0;
        }
    }

    /**
     * @return all user logins
     */
    /* SHSP-CR-0005 */
    @Override
    public Page<UserLogin> getUserDataList(PaginationVO paginationVO, Pageable pageable) {
        try {

            StringBuilder usersMainQuery = new StringBuilder();

            StringBuilder usersWhereClaus = new StringBuilder();
            StringBuilder usersGroupOrderBy = new StringBuilder();

            usersMainQuery.append(" select " + " usrl.userLoginId as userLoginId, " + "usrl.authToken as authToken," // 1
                    + " usrl.company as company, " + "usrl.ipAddress as ipAddress ," // 3
                    + " usrl.loginDate as loginDate," + " usrl.logoutDate as logoutDate, " // 5
                    + " usrl.status as status, " + "usrl.username as username," + " usr.firstName as firstName," // 8
                    + " usr.lastName as lastName, " + " usr.company.name as companyName " // 10
                    + " from UserLogin usrl, Users usr where ( usrl.username = usr.adUserName or usrl.username =  usr.email) and usrl.status = 'access violation'");

            if (paginationVO.getStartDate() != null || paginationVO.getEndDate() != null
                    || StringUtils.isNotEmpty(paginationVO.getAdUserName())) {
                usersWhereClaus.append(" AND");
            }

            if (paginationVO.getStartDate() != null) {
                usersWhereClaus.append(" CAST(usrl.loginDate AS DATE) >= :startDate");
                usersWhereClaus.append(" AND");
            }

            if (paginationVO.getEndDate() != null) {
                usersWhereClaus.append(" CAST(usrl.loginDate AS DATE) <= :endDate");
                usersWhereClaus.append(" AND");
            }

            if (StringUtils.isNotEmpty(paginationVO.getAdUserName())) {
                usersWhereClaus.append(" lower(usrl.username) like :adUserName");
                usersWhereClaus.append(" AND");
            }

            usersGroupOrderBy.append(" ORDER BY usrl.loginDate DESC");

            String where = "";
            if (!(usersWhereClaus.toString().equals("")))
                where = usersWhereClaus.toString().substring(0, usersWhereClaus.length() - 4);

            usersMainQuery.append(where);
            usersMainQuery.append(usersGroupOrderBy.toString());

            Query userLoginsDataQuery = getCrntSession().createQuery(usersMainQuery.toString());

            // Setting parameters
            if (paginationVO.getStartDate() != null) {
                userLoginsDataQuery.setParameter("startDate", paginationVO.getStartDate());
            }

            if (paginationVO.getEndDate() != null) {
                userLoginsDataQuery.setParameter("endDate", paginationVO.getEndDate());
            }

            if (StringUtils.isNotEmpty(paginationVO.getAdUserName())) {
                userLoginsDataQuery.setParameter("adUserName", "%" + paginationVO.getAdUserName().toLowerCase() + "%");
            }

            List<UserLogin> list = executeHQLSelectQuery(userLoginsDataQuery);

            if (pageable != null) {
                userLoginsDataQuery.setFirstResult((int) pageable.getOffset());
                userLoginsDataQuery.setMaxResults(pageable.getPageSize());
            }

            List<UserLogin> userDataList = executeHQLSelectQuery(userLoginsDataQuery);

            return new PageImpl<>(userDataList, pageable, Long.parseLong(((Integer) (list.size())).toString()));

        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
        return null;
    }

    @Override
    public List<Object[]> getUserDataListArray(PaginationVO paginationVO) {
        try {
            StringBuilder usersMainQuery = new StringBuilder();
            StringBuilder usersWhereClaus = new StringBuilder();
            StringBuilder usersGroupOrderBy = new StringBuilder();
            usersMainQuery.append(" select " + " usrl.userLoginId as userLoginId, " + "usrl.authToken as authToken," // 1
                    + " usrl.company as company, " + "usrl.ipAddress as ipAddress ," // 3
                    + " usrl.loginDate as loginDate," + " usrl.logoutDate as logoutDate, " // 5
                    + " usrl.status as status, " + "usrl.username as username," + " usr.firstName as firstName," // 8
                    + " usr.lastName as lastName," + " usr.company.name as companyName " // 10
                    + " from UserLogin usrl, Users usr where ( usrl.username = usr.adUserName or usrl.username =  usr.email) and usrl.status = 'access violation'");
            if (StringUtils.isNotEmpty(paginationVO.getAdUserName())) {
                usersMainQuery.append(" and lower(usr.adUserName) like ('%" + paginationVO.getAdUserName().toLowerCase() + "%')");
            }
            if (paginationVO.getStartDate() != null) {
                usersMainQuery.append(" and CAST(usrl.loginDate AS DATE) >= :startDate");
            }
            if (paginationVO.getEndDate() != null) {
                usersMainQuery.append(" and CAST(usrl.loginDate AS DATE) <= :endDate");
            }
            usersGroupOrderBy.append(" ORDER BY usrl.loginDate DESC");

            String where = "";
            if (!(usersWhereClaus.toString().isEmpty()))
                where = usersWhereClaus.toString().substring(0, usersWhereClaus.length() - 4);

            usersMainQuery.append(where);
            usersMainQuery.append(usersGroupOrderBy.toString());

            Query userLoginsDataQuery = getCrntSession().createQuery(usersMainQuery.toString());
            if (paginationVO.getStartDate() != null) {
                userLoginsDataQuery.setParameter(ApplicationConstants.START_DATE, paginationVO.getStartDate());
            }
            if (paginationVO.getEndDate() != null) {
                userLoginsDataQuery.setParameter(ApplicationConstants.END_DATE, paginationVO.getEndDate());
            }

            return executeHQLSelectQuery(userLoginsDataQuery);

        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
        return Collections.emptyList();

    }

    @Override
    public void updateUserLogin(String adUserName) {
        try {

            StringBuilder userLoginString = new StringBuilder();

            userLoginString.append("update user_login set is_login = false, status = 'logged off', logout_date = '"
                    + new Date() + "' where lower(username) ='" + adUserName.toLowerCase() + "' and is_login = true");
            logger.info(userLoginString.toString());
            NativeQuery<UserLogin> query = getSQLQueryObj(userLoginString.toString());
            query.executeUpdate();

        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);

        }

    }

}
