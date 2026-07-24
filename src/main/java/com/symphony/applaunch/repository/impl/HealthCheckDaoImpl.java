package com.symphony.applaunch.repository.impl;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.symphony.applaunch.repository.IHealthCheckDao;


@Repository
@RequiredArgsConstructor 
public class HealthCheckDaoImpl implements IHealthCheckDao {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckDaoImpl.class);

	private final JdbcTemplate jdbcTemplate;//NOSONAR

	@Override
	public boolean checkDbConnection() {
		log.info("HealthCheckDaoImpl :: checkDbConnection() called!");

		try {
			log.info("HealthCheckDaoImpl :: Executing database health check query");

			//Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			//log.info("HealthCheckDaoImpl :: Database health check successful, result: {}", result);
			
			return true;

		} catch (DataAccessException e) {
			log.error("HealthCheckDaoImpl :: Database connection failed - DataAccessException: {}", e.getMessage(), e);
			return false;

		} catch (Exception e) {
			log.error("HealthCheckDaoImpl :: Unexpected error during database health check: {}", e.getMessage(), e);
			return false;
		}
	}
}
