package com.symphony.applaunch.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.constants.LogEventTypeConstant;
import com.symphony.applaunch.dto.ResponseDTO;
import com.symphony.applaunch.dto.SHSAppDto;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.repository.IAppLaunchDao;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.impl.AppServiceImpl;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.ConversionUtil;
import com.symphony.applaunch.util.EncryptionUtil;
import com.symphony.applaunch.util.LoggingUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final LoggingUtility loggingUtility;

	private static final Logger logger = LoggerFactory.getLogger(AppLaunchController.class);

	private ConversionUtil convertDtoToEntity = new ConversionUtil();

	private final Environment environment;

	private final IAppLaunchDao appLaunchDAO;

	private final ClientUtil clientUtil;

	private final UserService userService;

	private final EncryptionUtil encryptionUtil;

	private final AppLaunchController appLaunchController;

	private final AppServiceImpl appService;

	@RequestMapping(value = "/appRedirection", method = { RequestMethod.POST, RequestMethod.GET }) // NOSONAR
	public ResponseEntity<String> authHandler(HttpServletRequest request, HttpServletResponse response) {

		logger.info("........Authentication handler........");
		Users userData;

		String header = request.getHeader("Authorization");
		validateHeader(header);

		String token = EncryptionUtil.decryptData(header.substring(7));
		userData = userService.getUserFromToken(token);

		logger.info("........User " + userData.getAdUserName() + " Authenticate Successfully");

		List<AppLaunch> applaunchList = appLaunchDAO.getCurrentAppLaunch(userData.getAdUserName());

		if (!applaunchList.isEmpty()) {
			AppLaunch appLaunch = applaunchList.get(0);
			String appId = appLaunch.getToken() != null ? appLaunch.getToken() : "";

			if (appId.contains("SL"))
				appId = appId.replace("SL", "/");
			if (appId.contains("PL"))
				appId = appId.replace("PL", "+");

			try {
				appId = encryptionUtil.decryptData1(appId);
			} catch (Exception e) {
				logger.error(ApplicationConstants.CATCH_MESSAGE + e);
				return new ResponseEntity<>(
						environment.getProperty(ApplicationConstants.SERVER_URL) + "/#/login?login_error=23",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			SHSApp appObj = appService.getAppById(appId);

			SHSAppDto shsappDto = convertDtoToEntity.shsAppEntityToDto(appObj);

			logger.info("........App name for shortcut URL........" + appObj.getName());

			ResponseEntity<ResponseDTO> responseDTO = appLaunchController.verifyUserApp(appLaunch.getUser().getId(),
					shsappDto);

			ResponseDTO dto = responseDTO.getBody();
			Boolean isSuccess = false;
			if (dto != null) {
				isSuccess = dto.getIsSuccess();
			}

			if (Boolean.TRUE.equals(isSuccess)) {
				return getStringResponseEntity(request, appLaunch, appObj, token);
			} else {
				return getEntity(appLaunch);
			}

		} else {
			return new ResponseEntity<>(
					environment.getProperty(ApplicationConstants.SERVER_URL) + "/#/portal/dashboard", HttpStatus.OK);
		}

	}

	@NotNull
	private ResponseEntity<String> getEntity(AppLaunch appLaunch) {
		appLaunch.setUrl("used");
		appLaunchDAO.saveAppLaunch(appLaunch);
		return new ResponseEntity<>(
				environment.getProperty(ApplicationConstants.SERVER_URL) + "/#/login?login_error=17", HttpStatus.OK);
	}

	@NotNull
	private ResponseEntity<String> getStringResponseEntity(HttpServletRequest request, AppLaunch appLaunch,
			SHSApp appObj, String token) {

		 logEvent(request, appLaunch, appObj);
		try {
			return new ResponseEntity<>(environment.getProperty(ApplicationConstants.SERVER_URL)
					+ "symphonyweb/service/apps/redirect?appId=" + appObj.getId() + "&appName="
					+ appObj.getCustomAppType() + "&userId=" + appLaunch.getUser().getId() + "&Authorization=" + token
					+ "&redirectUrl=" + URLEncoder.encode(appObj.getUrl(), StandardCharsets.UTF_8.toString()),
					HttpStatus.OK);
		} catch (UnsupportedEncodingException e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + e);
			return new ResponseEntity<>(
					environment.getProperty(ApplicationConstants.SERVER_URL) + "/#/login?login_error=urlEncodingError",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static void validateHeader(String header) {
		if (header == null) {
			throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
					HttpStatus.UNAUTHORIZED);
		}

		if (!StringUtils.hasText(header) && !header.startsWith("Bearer")) {
			throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
					HttpStatus.UNAUTHORIZED);
		}
	}

	private void logEvent(HttpServletRequest request, AppLaunch appLaunch, SHSApp appObj) {

		try {
			String browserType = clientUtil.getClientBrowser(request);
			String ipAddress = loggingUtility.getIpAddr(request);
			loggingUtility.logUserEvent(appLaunch.getUser(),
					appObj.getName() + LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG + " using shortcut URL "
							+ appLaunch.getUser().getAdUserName(),
					LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER, browserType, appObj.getId().longValue(), null,
					ipAddress);
			appLaunch.setUrl("used");
			appLaunchDAO.saveAppLaunch(appLaunch);

		} catch (Exception e) {
			logger.error(ApplicationConstants.CATCH_MESSAGE + "{}", e);
		}

	}

}
