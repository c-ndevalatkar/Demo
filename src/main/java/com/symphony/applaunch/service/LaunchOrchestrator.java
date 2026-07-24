package com.symphony.applaunch.service;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.constants.LogEventTypeConstant;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.EncryptionUtil;
import com.symphony.applaunch.util.LoggingUtility;
import com.symphony.applaunch.util.UserValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LaunchOrchestrator {

    @Value("${shs.portal.url}")
    private String shsPortalUrl;

    private static final Logger logger = LoggerFactory.getLogger(LaunchOrchestrator.class);

    private final Map<String, LaunchStrategy> strategies;
    private final UserService userService;
    private final UserValidationUtil userValidationUtil;
    private final IAppService appService;
    private final ClientUtil clientUtil;
    private final LoggingUtility loggingUtility;
    private final AppStrategyUtil appStrategyUtil;

    @Autowired
    public LaunchOrchestrator(
            Map<String, LaunchStrategy> strategies, UserService userService, UserValidationUtil userValidationUtil, IAppService appService, ClientUtil clientUtil, LoggingUtility loggingUtility, AppStrategyUtil appStrategyUtil) {
        this.strategies = strategies;
        this.userService = userService;
        this.userValidationUtil = userValidationUtil;
        this.appService = appService;
        this.clientUtil = clientUtil;
        this.loggingUtility = loggingUtility;
        this.appStrategyUtil = appStrategyUtil;
    }

    public void launch(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        logger.info("LaunchOrchestrator :: launch() called!");

        String appId = null;
        String appName = null;
        String redirectUrl = null;
        Long userId = null;
        String token = null;

        try {

            userId = Long.parseLong(request.getParameter(ApplicationConstants.USER_ID));
            logger.info("LaunchOrchestrator ::launch():: USER_DATA :: USER_ID :: {}", userId);
            token = EncryptionUtil.decryptData(request.getParameter(ApplicationConstants.AUTHORIZATION));
            logger.info("LaunchOrchestrator ::launch():: Print token for userId :: {}", userId);

            redirectUrl = java.net.URLDecoder.decode(request.getParameter(ApplicationConstants.REDIRECT_URL),
                    ApplicationConstants.UTF_MSG);

            if (request.getParameter(ApplicationConstants.APP_ID) != null) {

                logger.info("LaunchOrchestrator :: launch() :: APP_ID -- {}", request.getParameter(ApplicationConstants.APP_ID));

                appId = (request.getParameter(ApplicationConstants.APP_ID));
                logger.info("appid === {}", appId);
            }

            if (request.getParameter(ApplicationConstants.APPNAME) != null) {
                logger.info("LaunchOrchestrator :: launch() :: APP_NAME--  {}", request.getParameter(ApplicationConstants.APPNAME));

                logger.info(request.getParameter(ApplicationConstants.APPNAME));
                appName = request.getParameter(ApplicationConstants.APPNAME);
            }

            if (request.getParameter(ApplicationConstants.REDIRECT_URL) != null) {
                logger.info("LaunchOrchestrator :: launch() :: redirectURL--  {}", request.getParameter(ApplicationConstants.REDIRECT_URL));

                redirectUrl = request.getParameter(ApplicationConstants.REDIRECT_URL);
            }

            userId = Long.parseLong(request.getParameter(ApplicationConstants.USER_ID));

        } catch (Exception e) {
            logger.info("LaunchOrchestrator :: launch() :: catch block called!");
            logger.error(ApplicationConstants.CATCH_MESSAGE + e);
            appStrategyUtil.redirectToErrorPage("8", "", "", httpServletResponse);
        }

        Users loggedInuserEntity = null;
        try {
            loggedInuserEntity = userService.getUserFromToken(token);
        } catch (ApplicationException e) {
            httpServletResponse.sendRedirect(shsPortalUrl + "/#/login?login_error=23");
        }

        if (!userValidationUtil.isUserSameAsTokenUser(userId, loggedInuserEntity)) {
            logger.info("AppController :: appRedirect() - check UserValidation block");
            logger.info("UserID :::: {}", userId);
            logger.info("loggedInuserEntity :::: {}", loggedInuserEntity);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ApplicationConstants.UNAUTHORIZED_ACCESS);
        }

        SHSApp appObj = appService.getAppById(appId);
        logger.info("Appcontroller :: appRedirect() :: App_Object --  " + appObj.getId());

        if (!appObj.getUrl().equalsIgnoreCase(redirectUrl)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ApplicationConstants.BAD_REQUEST);
        }

        /*
         * saving a log for Application launch by user.
         */
        try {
            logger.info("LaunchOrchestrator :: launch() :: Save Log block");
            logger.info("loggedInuser received:" + loggedInuserEntity);
            request.setAttribute(ApplicationConstants.LOGGED_IN_USER, loggedInuserEntity);
            String browserType = clientUtil.getClientBrowser(request);
            logger.info("browserType received =" + browserType);
            String ipAddress = loggingUtility.getIpAddr(request);
            loggingUtility.logUserEvent(loggedInuserEntity, appObj.getName() + LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG
                            + loggedInuserEntity.getAdUserName(),
                    LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER, browserType, Long.parseLong(appId), null, ipAddress);
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }

        try {
            String strategyKey = appName;
            if (appName != null && (appName.equalsIgnoreCase(ApplicationConstants.GENERIC) || appName.equalsIgnoreCase(ApplicationConstants.DATASTEWARD))) {
                strategyKey = "GENERIC";
            }
            logger.info("LaunchOrchestrator :: launch():: All Strategies :: {}", strategies);
            logger.info("LaunchOrchestrator :: launch():: Strategy Key :: {}", strategyKey);
            LaunchStrategy strategy = strategies.get(strategyKey.toUpperCase());
            logger.info("LaunchOrchestrator :: launch():: ready to launch Strategy Key :: {}", strategy);

            if (strategy == null) {
                try {
                    httpServletResponse.sendRedirect(redirectUrl);
                } catch (IOException e) {
                    logger.error(ApplicationConstants.CATCH_MESSAGE + e);

                }
            } else {
                strategy.execute(loggedInuserEntity, redirectUrl, appObj, request, httpServletResponse);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
