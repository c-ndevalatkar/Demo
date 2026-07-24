package com.symphony.applaunch.controller;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.AppLaunchDto;
import com.symphony.applaunch.dto.ResponseDTO;
import com.symphony.applaunch.dto.SHSAppDto;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.service.LaunchOrchestrator;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.impl.AppServiceImpl;
import com.symphony.applaunch.util.ConversionUtil;
import com.symphony.applaunch.util.EncryptionUtil;
import com.symphony.applaunch.util.JWTBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class AppLaunchController {

    private static final Logger logger = LoggerFactory.getLogger(AppLaunchController.class);

	private final LaunchOrchestrator orchestrator;
	private final JWTBuilder jwtBuilder;
	private final ConversionUtil convertDtoToEntity;
	private final UserService userService;
	private final AppServiceImpl appService;

	@Autowired
	public AppLaunchController(LaunchOrchestrator orchestrator, JWTBuilder jwtBuilder, UserService userService,
			ConversionUtil conversionUtil, AppServiceImpl appService) {
		this.orchestrator = orchestrator;
		this.jwtBuilder = jwtBuilder;
		this.convertDtoToEntity = conversionUtil;
		this.userService = userService;
		this.appService = appService;
	}

    /**
     * Accepts a launch request (appId OR appName + redirectUrl + userId + Authorization header value)
     * Returns a HTTP 302 redirect by setting Location header (or returns JSON error).
     *
     * @return
     */
    @PostMapping (value = "/launch")
    public void launch(HttpServletRequest request, HttpServletResponse servletResponse) throws Exception {

        logger.info("AppLaunchController:: launch() called!");
        logger.info(request.getParameter(ApplicationConstants.REDIRECT_URL)); /* NOSONAR */
        String tokenValue = null;
        String token = EncryptionUtil.decryptData(request.getParameter(ApplicationConstants.AUTHORIZATION));
        logger.info("Token fetched from the request:{}", token);
        String header = request.getHeader(ApplicationConstants.AUTHORIZATION);
        if (header == null) {
            logger.warn("AppLaunchController:: launch() - Authorization header MISSING");
        } else {
            String masked = header.length() > 12 ? header.substring(0,6) + "..." + header.substring(header.length()-4) : header;
            logger.info("AppLaunchController:: launch() - Authorization header present (masked) = {}", masked);
            tokenValue = header.startsWith("Bearer ") ? header.substring(7) : header;
        }

        try {
            if(token != null) {

                // only decrypt if you actually store encrypted values; otherwise use tokenValue directly
                token = tokenValue;
                logger.info("Token fetched from the header:{}", token);
            }
        } catch (Exception e) {
            logger.error("Failed to decrypt token", e);
            token = null;
        }

        // validate JWT token string passed by portal (bearer or raw)
        if(jwtBuilder.validateToken(token, false) == null) {
            throw new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
                    HttpStatus.UNAUTHORIZED);
        }

        // perform launch orchestration
        orchestrator.launch(request, servletResponse);

		logger.info("Application launched successfully!");
	}

	/**
	 * This method is used to handle the launch app.
	 */
	@PostMapping(value = "/launchApp")
	public ResponseEntity<AppLaunch> launchApp(@RequestBody AppLaunchDto appLaunchObjDto, HttpServletResponse response,
			HttpServletRequest request) {

		AppLaunch appEntity = convertDtoToEntity.appLaunchDtoToEntity(appLaunchObjDto);

		AppLaunch appLaunch = new AppLaunch();

		Users user = userService.getUserByEmailId(appEntity.getUserName());
		if (user != null) {
			appLaunch.setToken(appEntity.getToken());
			appLaunch.setLaunchTime(new Date());
			appLaunch.setUser(user);
			appLaunch = userService.saveAppLaunch(appLaunch);
		} else {
			user = userService.getUserByAdUsername(appEntity.getUserName());
			if (user != null) {
				appLaunch.setToken(appEntity.getToken());
				appLaunch.setLaunchTime(new Date());
				appLaunch.setUser(user);
				appLaunch = userService.saveAppLaunch(appLaunch);
			} else {
				throw new ApplicationException(ApplicationConstants.USER_NOT_FOUND + appEntity.getUserName(),
						ErrorCode.USER_NOT_EXISTS.getCodeId(), HttpStatus.NOT_ACCEPTABLE);
			}
		}
		return new ResponseEntity<>(appLaunch, HttpStatus.OK);
	}

	/**
	 * This method is used to verify user app subscription
	 * 
	 * @param user id
	 * @param app  id
	 * @return ResponseEntity response object having updated shsApp object
	 */
	@PostMapping(value = "/verifyUserApp")
	public ResponseEntity<ResponseDTO> verifyUserApp(Long userId, SHSAppDto shsApp) {

		SHSApp entity;
		entity = convertDtoToEntity.shsAppDtoToEntity(shsApp);

		ResponseDTO responseDTO = new ResponseDTO();
		UserApp userApps = appService.getVerifyApps(userId, entity);

		if (userApps != null) {
			responseDTO.setIsSuccess(true);
			responseDTO.setMessage("User has a access to app");
		} else {
			responseDTO.setIsSuccess(false);
			responseDTO.setMessage("No access to app");
		}

		return new ResponseEntity<>(responseDTO, HttpStatus.OK);
	}

}
