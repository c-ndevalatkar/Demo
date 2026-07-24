package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.constants.LogEventTypeConstant;
import com.symphony.applaunch.dto.PaginationVO;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.IAppService;
import com.symphony.applaunch.service.LaunchStrategy;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.LoggingUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;


@Service("MICROSTRATEGY")
public class MstrStrategy implements LaunchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MstrStrategy.class);

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserService userService;
    private final IAppService appService;
    private final ClientUtil clientUtil;
    private final LoggingUtility loggingUtility;
    private final AppStrategyUtil appStrategyUtil;

    public MstrStrategy(UserService userService, IAppService appService, ClientUtil clientUtil, LoggingUtility loggingUtility, AppStrategyUtil appStrategyUtil) {
        this.userService = userService;
        this.appService = appService;
        this.clientUtil = clientUtil;
        this.loggingUtility = loggingUtility;
        this.appStrategyUtil = appStrategyUtil;
    }

    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("MicroStrategyStrategy :: execute() MSTR Application ");

        // microstrategy Sso
        loggedInuserEntity.setTokenType("MSTR");

        // generate a random token
        StringBuilder textBildr = new StringBuilder();
        String text = "";
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 20; i++) {
            textBildr.append(possible.charAt(secureRandom.nextInt(possible.length())));
            text = textBildr.toString();
        }

        String tokenCustom = text;
        loggedInuserEntity.setToken(text);

        PaginationVO paginationVo = new PaginationVO();

        paginationVo.setUser(loggedInuserEntity);
        paginationVo.setAppId(app.getId().toString());
        paginationVo.setAppname(app.getName());



        // redirect to url with generated ticket
        try {

            // update the token in user table
            saveMicroStrategyTokenVerification(paginationVo, request);

            logger.info("Recdirect Url" + redirectUrl);
            httpServletResponse.sendRedirect(redirectUrl + "&token=" + tokenCustom);
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            appStrategyUtil.redirectToErrorPage("0", e.getMessage(), ApplicationConstants.MICROSTRATEGY, httpServletResponse);
        }
    }

    /**
     * This method is used to save token for MicroStrategy.
     *
     * @param paginationVO User, Appname
     * @param request      HttpServletRequest
     */
    public void saveMicroStrategyTokenVerification(PaginationVO paginationVO, HttpServletRequest request) {

        userService.saveRandomGeneratedToken(paginationVO.getUser(), paginationVO.getUser().getToken());
        /*
         * saving a log for Application launch by user.
         */
        try {
            String browserType = clientUtil.getClientBrowser(request);
            Users loggedInuser = userService.getUserFromToken(request);
            String ipAddress = loggingUtility.getIpAddr(request);

            loggingUtility.logUserEvent(loggedInuser, appService.getAppById(paginationVO.getAppId()).getName()
                            + LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG + loggedInuser.getAdUserName(),
                    LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER, browserType, Long.parseLong(paginationVO.getAppId()), null, ipAddress);
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            throw new RuntimeException(e);// NOSONAR
        }
    }
}
