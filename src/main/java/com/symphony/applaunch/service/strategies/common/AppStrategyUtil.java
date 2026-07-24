package com.symphony.applaunch.service.strategies.common;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.util.JWTBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppStrategyUtil {

    @Value("${shs.portal.url}")
    private String shsPortalUrl;

    private static final Logger logger = LoggerFactory.getLogger(AppStrategyUtil.class);

    private final UserService userService;
    private final JWTBuilder jwtBuilder;

    public AppStrategyUtil(UserService userService, JWTBuilder jwtBuilder) {
        this.userService = userService;
        this.jwtBuilder = jwtBuilder;
    }

    /**
     * method is used to generate the MDT ticket
     *
     * @param user    loggedInuserEntity
     * @param request http request
     * @return web ticket or empty string
     */

    public ResponseEntity<Map<String, String>> generateWebTicket(Users user, HttpServletRequest request) {
        logger.info(" generateWebTicket() called!");

        String webTicket = "";
        logger.info("Web Ticket.........{}", "WebTicket-XXXX");
        webTicket = createSHSToken(user);
        Map<String, String> ticketUrl = new HashMap<>();
        ticketUrl.put("ticket", webTicket);
        return new ResponseEntity<>(ticketUrl, HttpStatus.OK);
    }

    /**
     * method is privately used to process datasteward request
     */
    private String createSHSToken(Users userData) {
        logger.info(" createSHSToken() called!");

        logger.info("create JWT token for datasteward request");
        String token = jwtBuilder.createJWT(userData.getEmail(), userData.getAdUserName(), userData.getSsoAppId(), true);
        userData.setToken(token);


        userService.saveRandomGeneratedToken(userData, token);
        return token;
    }


    public void redirectToErrorPage(String errorCode, String errorMessage, String project,
                                    HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse
                    .sendRedirect(shsPortalUrl + ApplicationConstants.ERROR_PAGE + "?ErrorCode="
                            + errorCode + "&ErrorMessage=" + errorMessage + "&Project=" + project);
        } catch (IOException e) {

            logger.error(ApplicationConstants.CATCH_MESSAGE + e);
        }
    }
}
