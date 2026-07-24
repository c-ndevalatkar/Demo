package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.DimensionDTO;
import com.symphony.applaunch.entity.MdmRole;
import com.symphony.applaunch.entity.UserMdmDimension;
import com.symphony.applaunch.entity.UserRoles;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.repository.GenericDAO;
import com.symphony.applaunch.repository.IUserDAO;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Repository("userDAOImpl")
public class UserDAOImpl extends GenericDAO<Users, Long> implements IUserDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

	public UserDAOImpl() {
		super(Users.class);
	}

	/**
	 * method is used to find whether userid/email id is already registered
	 *
	 * @param emailId
	 * @return {@link Users}
	 */
	@Override
	public Users findByEmail(String emailId) {
		logger.info("UserDaoImpl :: findByEmail() called!");

		try {
			Query<Users> userDataQuery = getQueryFromNamedQuery("Users.findByEmail");
			userDataQuery.setParameter(ApplicationConstants.EMAILID, emailId.toLowerCase());
			return executeUniqueResultHqlQuery(userDataQuery);
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
			throw new ApplicationException(ApplicationConstants.MULTIPLE_EMAILS + emailId,
					ErrorCode.MULTIPLE_EMAILS.getCodeId(), HttpStatus.NOT_ACCEPTABLE);
		}

	}

	/**
	 * method is used to find user by id
	 *
	 * @param id
	 * @return {@link Users}
	 */
	@Override
	public Users findOne(Long id) {
		logger.info("UserDaoImpl :: findOne() called!");
		Query<Users> findOne = getQueryFromNamedQuery("Users.findOne");
		findOne.setParameter("id", id);
		return executeUniqueResultHqlQuery(findOne);

	}

	/**
	 * method is used to save user info to database
	 *
	 * @param user
	 * @return
	 */
	@Override
	public Users saveUserData(Users user) {
		try {
			save(user);
		} catch (Exception e) {
			logger.info("context", e);
		}
		return user;
	}

	/**
	 * method is used to update user info to database
	 *
	 * @param user
	 * @return
	 */

	@Override
	public Users updateUserData(Users user) {
		try {
			update(user);
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
		}
		return user;
	}

	/**
	 * this method used to find verified users from database.
	 *
	 * @param pageable
	 * @return verified user list
	 */

	@Override
	public Page<Users> findAllTokenVerifiedUsers(Pageable pageable) {

		try (Session session = getCrntSession()) {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Users> query = builder.createQuery(Users.class);
			Root<Users> root = query.from(Users.class);

			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
			Root<Users> countRoot = countQuery.from(Users.class);
			countQuery.select(builder.count(countRoot));

			query.orderBy(builder.asc(root.get("firstName")));

			Long totalCount = session.createQuery(countQuery).getSingleResult();
			List<Users> userDataList = session.createQuery(query).setFirstResult((int) pageable.getOffset())
					.setMaxResults(pageable.getPageSize()).getResultList();

			userDataList.forEach(element -> element
					.setIsActive(element.getEndDate() == null || !(element.getEndDate().before(new Date()))));

			return new PageImpl<>(userDataList, pageable, totalCount);
		}
	}

	/**
	 * this method used to delete recoed by userId
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public Long deleteByUserId(Long userId) {
		try {
			Users userImage = null;
			delete(userImage);
			return userId;
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
			return null;
		}
	}

	/**
	 * method is used to find user info by adName from database
	 *
	 * @param adName
	 * @return {@link Users}
	 */

	@Override
	public Users findByAdName(String adName) {
		logger.info("UserDaoImpl :: findByAdName() called!");

		try {

			Query<Users> userDataQuery = getQueryFromNamedQuery("Users.findByAdName");
			userDataQuery.setParameter(ApplicationConstants.AD_NAME, adName.toLowerCase());

			return executeUniqueResultHqlQuery(userDataQuery);
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
			throw new ApplicationException(ApplicationConstants.MULTIPLE_AD_USERNAME + adName,
					ErrorCode.MULTIPLE_AD_USERNAMES.getCodeId(), HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * method is used to verify token for particular user.
	 *
	 * @param token
	 * @return {@link Users}
	 */

	@Override
	public Users verifyToken(String token) {
		logger.info("UserDAOImpl :: verifyToken() called!");
		logger.info("UserDAOImpl :: verifyToken():: Print token {}", token);
		Query<Users> userDataQuery = getQueryFromNamedQuery("Users.verifyToken");
		userDataQuery.setParameter("token", token);
		userDataQuery.setParameter("expirationTime", ApplicationConstants.EXPIRATION_THREE_MIN_TOKEN);
		return executeUniqueResultHqlQuery(userDataQuery);
	}

	/**
	 * method is used to find user info by according to role from database
	 *
	 * @param role
	 * @return {@link Users}
	 */
	@Override
	public List<Users> findUsersByRole(UserRoles role) {

		Query query = getQueryFromNamedQuery("Users.findUsersByRole");
		query.setParameter("role", role);
		return executeHQLSelectQuery(query);
	}

	/**
	 * method is used to find global user for particular company.
	 *
	 * @param companyId,userToken
	 * @return list {@link Users}
	 */
	@Override
	public List<Users> getGlobalUsersByCompany(Long companyId, String userToken) {

		try {
			StringBuilder userData = new StringBuilder();
			userData.append("FROM Users user ");
			if (userToken != null) {
				if (userToken.contains(" ")) {
					userToken = userToken.toLowerCase();
					userData.append("WHERE user.company.id = " + companyId
							+ " AND (user.endDate >= CURRENT_DATE or user.endDate is null) and lower(user.firstName) like '%"
							+ userToken.split(" ")[0] + "%'");
					userData.append(" AND lower(user.lastName) like '%" + userToken.split(" ")[1] + "%')");
				} else {
					userToken = "'%" + userToken.toLowerCase() + "%'";
					userData.append("WHERE  user.company.id = " + companyId
							+ " AND (user.endDate >= CURRENT_DATE or user.endDate is null) and ((lower(user.firstName) like "
							+ userToken + ")");
					userData.append(" OR (lower(user.lastName) like " + userToken + ")");
					userData.append(" OR (lower(user.email) like " + userToken + ")");
					userData.append(" OR (lower(user.adUserName) like " + userToken + ")");
					userData.append(" OR (lower(concat(user.firstName,' ',user.lastName)) like " + userToken + "))");
				}
			}

			userData.append(" ORDER BY user.firstName, user.lastName");
			Query userDataQuery = getCrntSession().createQuery(userData.toString());/* NOSONAR */

			return executeHQLSelectQuery(userDataQuery);
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
			return Collections.emptyList();
		}

	}

	@Override
	public List<String> getDimentionByUserId(long userId) {

		try {
			StringBuilder usersDimensionDashboardData = new StringBuilder();
			usersDimensionDashboardData.append(
					"select d.dimensionName from Dimension as d , UsersDimensionDashboard as ud where ud.dimension=d.id and ud.user =:userId  ");

			Query<String> usersDimensionDashboardDataQuery = getCrntSession()
					.createQuery(usersDimensionDashboardData.toString(), String.class);

			usersDimensionDashboardDataQuery.setParameter("userId", userId);
			return executeHQLSelectQuery(usersDimensionDashboardDataQuery);
		} catch (ApplicationException e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);

		}
		return Collections.emptyList();
	}

	@Override
	public List<DimensionDTO> findMdmDimensionsByUserId(String userId) {
		try {
			CriteriaBuilder criteriaBuilder = getCrntSession().getCriteriaBuilder();
			CriteriaQuery<DimensionDTO> criteriaQuery = criteriaBuilder.createQuery(DimensionDTO.class);
			Root<UserMdmDimension> userMdmDimensionRoot = criteriaQuery.from(UserMdmDimension.class);
			Join<UserMdmDimension, MdmRole> roleJoin = userMdmDimensionRoot.join("role", JoinType.LEFT);

			criteriaQuery.select(criteriaBuilder.construct(DimensionDTO.class,
					userMdmDimensionRoot.get("dimension").get("id").alias("id"),
					userMdmDimensionRoot.get("dimension").get("dimensionName").alias("dimensionName"),
					userMdmDimensionRoot.get("dimension").get("description").alias("description"),
					userMdmDimensionRoot.get("dimension").get("color").alias("color"),
					userMdmDimensionRoot.get("dimension").get("instanceCount").alias("instanceCount"),
					userMdmDimensionRoot.get("dimension").get("overrideCount").alias("overrideCount"),
					userMdmDimensionRoot.get("dimension").get("errorCount").alias("errorCount"),
					roleJoin.alias("role")));

			criteriaQuery.where(
					criteriaBuilder.and(criteriaBuilder.equal(userMdmDimensionRoot.get("user").get("id"), userId),
							criteriaBuilder.isFalse(userMdmDimensionRoot.get("isDeleted"))));

			TypedQuery<DimensionDTO> query = getCrntSession().createQuery(criteriaQuery);
			return query.getResultList();
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);
			return new ArrayList<>();
		}
	}

}
