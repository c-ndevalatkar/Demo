package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.MSTRAuthResult;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.*;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.LoggingUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;


@Service("MICROSTRATEGYCLOUD")
public class MstrCloudStrategy implements LaunchStrategy {

    @Value("${mstr.user.password}")
    private String mstrUserLoginPassword;

    private static final Logger logger = LoggerFactory.getLogger(MstrCloudStrategy.class);

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserService userService;
    private final IAppService appService;
    private final ClientUtil clientUtil;
    private final LoggingUtility loggingUtility;
    private final MstrAuthService mstrAuthService;
    private final AppStrategyUtil appStrategyUtil;

    public MstrCloudStrategy(UserService userService, IAppService appService, ClientUtil clientUtil, LoggingUtility loggingUtility, MstrAuthService mstrAuthService, AppStrategyUtil appStrategyUtil) {
        this.userService = userService;
        this.appService = appService;
        this.clientUtil = clientUtil;
        this.loggingUtility = loggingUtility;
        this.mstrAuthService = mstrAuthService;
        this.appStrategyUtil = appStrategyUtil;
    }

    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("MicroStrategyStrategy :: execute() MSTR Application ");
        logger.info("Recdirect Url" + redirectUrl);

        // redirect to url with generated ticket
        try {
            // Redirect the browser to the /launch-mstr endpoint // password will be stored in secret manager --loggedInuserEntity.getAdUserName()
            logger.info("MicroStrategyStrategy:: loginAndLaunch() called!"); //loggedInuserEntity.getAdUserName()
            logger.info("MicroStrategyStrategy:: loginAndLaunch() called with User: {}",loggedInuserEntity.getAdUserName());
            mstrAuthService.loginAndLaunch(loggedInuserEntity.getAdUserName().toLowerCase(), mstrUserLoginPassword, request, httpServletResponse);
        } catch (Exception e) {
            logger.info("MstrCloudStartegy : Redirecting to the mstr-error page!");
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            appStrategyUtil.redirectToErrorPage("0", e.getMessage(), ApplicationConstants.MICROSTRATEGY, httpServletResponse);
        }
    }

}
