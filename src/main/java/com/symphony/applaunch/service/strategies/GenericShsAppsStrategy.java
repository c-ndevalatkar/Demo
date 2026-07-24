package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.LaunchStrategy;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service("GENERIC")
public class GenericShsAppsStrategy implements LaunchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GenericShsAppsStrategy.class);

    private final AppStrategyUtil appStrategyUtil;

    public GenericShsAppsStrategy(AppStrategyUtil appStrategyUtil) {
        this.appStrategyUtil = appStrategyUtil;
    }


    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("GenericShsAppsStrategy :: execute() Generic Application ");

        String tokenType = "generic";
        String appId = (request.getParameter(ApplicationConstants.APP_ID));
        Long userId = Long.parseLong(request.getParameter(ApplicationConstants.USER_ID));

        loggedInuserEntity.setSsoAppId(appId);
        loggedInuserEntity.setTokenType(tokenType);

        logger.info("GenericShsAppsStrategy ::execute():: GENRIC :: USER_DATA :: USER_ID :: %d :: APP_ID -- %s".formatted(userId, loggedInuserEntity.getSsoAppId()));
        logger.info("GenericShsAppsStrategy ::execute():: GENRIC :: USER_DATA :: USER_ID :: %d :: TokenType -- %s".formatted(userId, loggedInuserEntity.getTokenType()));

        // generatewebticket using userObj for sso
        ResponseEntity<Map<String, String>> response = appStrategyUtil.generateWebTicket(loggedInuserEntity, request);
        Map<String, String> map = response.getBody();
        String webTicket = map != null ? map.get(ApplicationConstants.TICKET) : "";

        logger.info("GenericShsAppsStrategy ::execute():: GENRIC :: USER_DATA :: USER_ID :: {}", userId);

        logger.info("Redirect Url {}", redirectUrl);

        // redirect to url with generated ticket
        try {

            if (redirectUrl.contains("&params")) {

                String urlnew = redirectUrl.split("&")[0];
                String param = redirectUrl.split("&")[1];

                logger.info(urlnew + webTicket + "&" + param);
                httpServletResponse.sendRedirect(urlnew + webTicket + "&" + param);
            } else {
                logger.info(redirectUrl + webTicket);
                httpServletResponse.sendRedirect(redirectUrl + webTicket);
            }

        } catch (IOException e) {
            logger.info("GenericShsAppsStrategy ::execute():: GENRIC :: USER_DATA :: USER_ID :: " + userId + ":: Catch block called!");
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);

        }
    }

}
