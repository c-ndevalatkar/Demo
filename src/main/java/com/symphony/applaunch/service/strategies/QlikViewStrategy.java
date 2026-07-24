package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.LaunchStrategy;
import com.symphony.applaunch.util.QlikViewManagement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("QLIKVIEW")
public class QlikViewStrategy implements LaunchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(QlikViewStrategy.class);

    private final QlikViewManagement qlikViewManagement;

    public QlikViewStrategy(QlikViewManagement qlikViewManagement) {
        this.qlikViewManagement = qlikViewManagement;
    }

    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("AppController :: appRedirect() QLIKVIEW SSO");
        // QlickView Sso

        String host = null;
        String document = null;

        // split the document and host from redirect url
        if (redirectUrl.toLowerCase().split(ApplicationConstants.QV_SPLIT_RD_URL.toLowerCase()).length > 0) {
            host = redirectUrl.toLowerCase().split(ApplicationConstants.QV_SPLIT_RD_URL.toLowerCase())[0]
                    .split("://")[1];
        } else if (redirectUrl.indexOf(ApplicationConstants.QV_RD_URL_INDEX_OF) >= 0) {
            host = redirectUrl.toLowerCase().split(ApplicationConstants.QV_RD_URL_INDEX_OF.toLowerCase())[0]
                    .split("://")[1];
        }

        if (redirectUrl.indexOf("AccessPoint") >= 0) {

            if (redirectUrl.indexOf("%7C") >= 0) {
                document = redirectUrl.split("%7C")[1].split("&")[0] + ApplicationConstants.QV_RD_SPLIT_HOST + host;
            } else {
                document = redirectUrl.split("\\|")[1].split("&")[0] + ApplicationConstants.QV_RD_SPLIT_HOST + host;
            }
        } else if (redirectUrl.indexOf("document") >= 0) {

            try {
                document = redirectUrl.split(ApplicationConstants.QV_RD_SPLIT_DOCUMENT)[1].split("&")[0]
                        + ApplicationConstants.QV_RD_SPLIT_HOST + host;
            } catch (Exception e) {
                logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            }
            try {
                document = redirectUrl.split(ApplicationConstants.QV_RD_SPLIT_DOCUMENT)[1].split("%26")[0]
                        + ApplicationConstants.QV_RD_SPLIT_HOST + host;
            } catch (Exception e) {
                logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
            }
        }

        // generate the qlickview ticket
        ResponseEntity<Map<String, String>> response = getWebTicketAndValidate(request, document, host);
        Map<String, String> map = response.getBody();
        String ticketUrl = map == null ? "" : map.get(ApplicationConstants.TICKET_URL);
        logger.info(ApplicationConstants.LOG_QV_TICKET, ticketUrl);

        // redirect to generated url with ticket
        try {
            httpServletResponse.sendRedirect(ticketUrl);
        } catch (IOException e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
    }


    public ResponseEntity<Map<String, String>> getWebTicketAndValidate(HttpServletRequest request, String document, String host) {
        logger.info("get QlikView web ticket.......");
        String webTicket = "";
        logger.info("Qlickview document : {}", document);
        logger.info("Qlickview host : {}", host);

        // Generate Qlickview web ticket using user adusername and apllication url and
        // host
        Users loggedInUser = (Users) request.getAttribute(ApplicationConstants.LOGGED_IN_USER);
        webTicket = qlikViewManagement.getQlikViewWebTicket(loggedInUser.getAdUserName(), document, host);
        logger.info("QlikView webTicket.........{}", webTicket);
        Map<String, String> ticketUrl = new HashMap<>();
        ticketUrl.put(ApplicationConstants.TICKET_URL, webTicket);
        return new ResponseEntity<>(ticketUrl, HttpStatus.OK);
    }
}
