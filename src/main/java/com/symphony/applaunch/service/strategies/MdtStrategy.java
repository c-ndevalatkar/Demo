package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.LaunchOrchestrator;
import com.symphony.applaunch.service.LaunchStrategy;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service("MDT")
public class MdtStrategy implements LaunchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MdtStrategy.class);

    private final AppStrategyUtil appStrategyUtil;

    public MdtStrategy(AppStrategyUtil appStrategyUtil) {
        this.appStrategyUtil = appStrategyUtil;
    }

    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("MDTStrategy :: execute() MDT Application ");

        String appId = (request.getParameter(ApplicationConstants.APP_ID));
        Long userId = Long.parseLong(request.getParameter(ApplicationConstants.USER_ID));
        // set the token and app details

        loggedInuserEntity.setTokenType("MDT");
        loggedInuserEntity.setSsoAppId(appId);

        logger.info("MDTStrategy ::execute():: MDT :: USER_DATA :: USER_ID :: %d:: AppId -- %s".formatted(userId, appId));
        // generate webticket
        ResponseEntity<Map<String, String>> response = appStrategyUtil.generateWebTicket(loggedInuserEntity, request);

        Map<String, String> map = response.getBody();
        String ticketUrl = map == null ? "" : map.get(ApplicationConstants.TICKET);
        logger.info("Url : " + redirectUrl + ApplicationConstants.TOKEN_LOG_INFO, ticketUrl);

        logger.info("MDTStrategy ::execute():: MDT :: USER_DATA :: USER_ID :: %d:: Redirect URL -- %s".formatted(userId, redirectUrl));
        logger.info("MDTStrategy ::execute():: MDT :: USER_DATA :: USER_ID :: %d:: Token URL -- %s".formatted(userId, ticketUrl));

        try {
            httpServletResponse.sendRedirect(redirectUrl + ApplicationConstants.TOKEN_LOG_INFO + ticketUrl);
        } catch (IOException e) {
            logger.info("MDTStrategy execute() :: MDT :: Catch block called!");
            logger.info("MDTStrategy ::execute():: MDT :: USER_DATA :: USER_ID:: " + userId);
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            appStrategyUtil.redirectToErrorPage("0", e.getMessage(), ApplicationConstants.MDT, httpServletResponse);
        }
    }
}
