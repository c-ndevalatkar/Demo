package com.symphony.applaunch.service.impl;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.symphony.applaunch.repository.IHealthCheckDao;
import com.symphony.applaunch.service.IHealthCheckService;

@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements IHealthCheckService {

	private static final Logger log = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

	private final IHealthCheckDao healthcheckDao;

	@Override
	public boolean checkApplicationHealth() {
		log.info("HealthCheckServiceImpl :: checkApplicationHealth() called!");
		boolean dbHealth = healthcheckDao.checkDbConnection();
		log.info("HealthCheckServiceImpl :: Database health status: {}", dbHealth);
		return dbHealth;
	}
}
