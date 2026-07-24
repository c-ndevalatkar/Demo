package com.symphony.applaunch.repository.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.repository.GenericDAO;
import com.symphony.applaunch.repository.IAppLaunchDao;

import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Transactional
@Repository("appLaunchDaoImpl")
public class AppLaunchDaoImpl extends GenericDAO<AppLaunch, Long> implements IAppLaunchDao {

	private static final Logger logger = LoggerFactory.getLogger(AppLaunchDaoImpl.class);

	public AppLaunchDaoImpl() {
		super(AppLaunch.class);
	}

	@Override
	public AppLaunch saveAppLaunch(AppLaunch appLaunch) {
		logger.info("AppLaunchDaoImpl :: saveAppLaunch() called!");
		try {
			saveOrUpdate(appLaunch);
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);
		}
		return appLaunch;
	}

	@Override
	public List<AppLaunch> getCurrentAppLaunch(String username) {
		logger.info("AppLaunchDaoImpl :: getCurrentAppLaunch() called!");

		try {
			String hql = "SELECT a FROM AppLaunch a " + "WHERE (CURRENT_TIMESTAMP - a.launchTime) < :interval "
					+ "AND a.user.adUserName = :username " + "AND (a.url IS NULL OR a.url != 'used') "
					+ "ORDER BY a.launchTime DESC";

			Query query = getCrntSession().createQuery(hql, AppLaunch.class);
			query.setParameter("interval", java.time.Duration.ofMinutes(1));
			query.setParameter("username", username);

			return query.getResultList();
		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);
			return Collections.emptyList();
		}
	}

}
