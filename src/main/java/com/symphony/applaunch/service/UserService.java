package com.symphony.applaunch.service;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.entity.DimensionDTO;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.repository.IAppLaunchDao;
import com.symphony.applaunch.repository.IUserDAO;
import com.symphony.applaunch.util.JWTBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("userService")
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final JWTBuilder jwtBuilder;
	@SuppressWarnings("unused")
	private final ILoginService loginService;
	private final IUserDAO userDAO;
	private final IAppLaunchDao appLaunchDao;

	public UserService(JWTBuilder jwtBuilder, ILoginService loginService, IUserDAO userDAO,
			IAppLaunchDao appLaunchDAO) {
		this.jwtBuilder = jwtBuilder;
		this.loginService = loginService;
		this.userDAO = userDAO;
		this.appLaunchDao = appLaunchDAO;
	}

	public Users getUserFromToken(String token) {
		try {

			if (jwtBuilder.validateToken(token, false) == null) {
				logger.info("UserServiceImpl getUserFromToken() :: Invalid Token");
				throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
						HttpStatus.UNAUTHORIZED);
			}

			String authType = jwtBuilder.getValue(token, ApplicationConstants.COMPANY_AUTH_TYPE);

			logger.info("getUserFromToken(): authtype == {}", authType);

			if (authType.equalsIgnoreCase(ApplicationConstants.OKTA)) {
				String userId = jwtBuilder.getValue(token, ApplicationConstants.EMAIL_ID);
				logger.info("getUserByEmailId(): email == {}", userId);
				return getUserByEmailId(userId);
			} else {
				String userId = jwtBuilder.getValue(token, ApplicationConstants.AD_USER_NAME);
				logger.info("getUserFromToken(): adusername == {}", userId);
				return getUserByAdUsername(userId);
			}

		} catch (ApplicationException ae) {
			logger.error("ApplicationException in getUserFromToken: {}", ae.getMessage(), ae);
			throw ae;
		} catch (Exception e) {
			logger.info("-----Catch blcok from getUserFromToken() with String-----");
			logger.error(ApplicationConstants.CATCH_MESSAGE + e.getMessage());
			throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
					HttpStatus.UNAUTHORIZED);
		}
	}

	public Users getUserFromToken(HttpServletRequest request) {
		try {
			String header = request.getHeader(ApplicationConstants.AUTHORIZATION);
			logger.info("Header from getUserFromToken(request) method : {Header}", header);
			
			String token = null;
			if (header != null && header.startsWith("Bearer ")) {
				token = header.substring(7);
			} else {
				token = header; 
			}
			logger.info("Token from getUserFromToken(request) method : {Token}", token);

			if (jwtBuilder.validateToken(token, false) == null) {
				logger.info("Token Error : {}", ApplicationConstants.INVALID_TOKEN);

				throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
						HttpStatus.UNAUTHORIZED);
			}
			logger.info("After Token fetched: {}", token);

			String adUserName = jwtBuilder.getValue(token, ApplicationConstants.AD_USER_NAME);
			return getUserByAdUsername(adUserName);
		} catch (Exception e) {
			logger.info("-----Catch blcok from getUserFromToken() with HttpServletRequest-----");
			logger.error(ApplicationConstants.CATCH_MESSAGE + e.getMessage());
			throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
					HttpStatus.UNAUTHORIZED);
		}
	}

	public Users saveRandomGeneratedToken(Users user, String token) {
		Users userData = userDAO.findOne(user.getId());
		userData.setToken(token);
		userData.setTokenType(user.getTokenType());
		userData.setTokenGeneratedTimestamp(new Date());
		return userDAO.updateUserData(userData);
	}

	@Transactional
	public Users getUserByEmailId(String emailId) {
		return userDAO.findByEmail(emailId);
	}

	@Transactional
	public Users getUserByAdUsername(String adUserName) {
		return userDAO.findByAdName(adUserName);
	}

	@Transactional
	public AppLaunch saveAppLaunch(AppLaunch appLaunch) {
		return appLaunchDao.saveAppLaunch(appLaunch);
	}

	@Transactional
	public Users getUserDataById(Long id) {
		logger.info("userId = {}", id);
		try {
			Users user = userDAO.findOne(id);
			logger.info("user received = {}", user);
			if (user != null) {
				List<String> dimenstionStringList = userDAO.getDimentionByUserId(id);
				if (dimenstionStringList != null && !dimenstionStringList.isEmpty()) {
					user.setDimensions(dimenstionStringList);
				}

				if (user.getCompany().getGlobalAdmin() != null) {
					user.getCompany()
							.setGlobalAdminName(userDAO.findOne(user.getCompany().getGlobalAdmin()).getAdUserName());
				}

				List<DimensionDTO> userMdmDimensionList = userDAO.findMdmDimensionsByUserId(user.getId().toString());
				if (userMdmDimensionList != null && !(userMdmDimensionList.isEmpty())
						&& !userMdmDimensionList.isEmpty())
					user.setIsDatasteward(true);
				user.setMdmDimensions(userMdmDimensionList);
				user.setIsActive(user.getEndDate() == null || !user.getEndDate().before(new Date()));
			}

			return user;
		} catch (Exception e) {
			logger.info("-----Catch blcok from getUserDataById() -----");
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);
			return new Users();
		}

	}

}
