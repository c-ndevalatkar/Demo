package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.LaunchStrategy;
import com.symphony.applaunch.util.QlikViewManagement;
import jakarta.servlet.ServletContext;
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

@Service("QLIKSENSE")
public class QlikSenseStrategy implements LaunchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(QlikSenseStrategy.class);

    private final QlikViewManagement qlikViewManagement;

    public QlikSenseStrategy(QlikViewManagement qlikViewManagement) {
        this.qlikViewManagement = qlikViewManagement;
    }

    @Override
    public void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.info("Appcontroller appRedirect() QLIKSENSE SSO");
        logger.info("Ticket generation for qliksense application..");
        // QlikSense Sso
        String host = null;

        host = redirectUrl.toLowerCase().split("://")[1].split("/")[0];
        String virtualProxy = redirectUrl.split(ApplicationConstants.DOT_COM)[1].split("/")[0];

        // get Qlicksense ticket using host
        ResponseEntity<Map<String, String>> response = new ResponseEntity<>(
                getQlikSenseWebTicketURL(request, virtualProxy, host, loggedInuserEntity), HttpStatus.OK);
        Map<String, String> map = response.getBody();
        String ticketUrl = map == null ? "" : map.get(ApplicationConstants.TICKET);
        logger.info(ApplicationConstants.LOG_QV_TICKET, ticketUrl);
        logger.info(" Url :"+ redirectUrl + ApplicationConstants.QS_TICKET_RD_URL, ticketUrl);
        try {
            httpServletResponse.sendRedirect(redirectUrl + ApplicationConstants.QS_TICKET_RD_URL + ticketUrl);
        } catch (IOException e) {
            logger.error("Exception occured while redirecting to qliksense application :",  e);
        }
    }

    private Map<String, String> getQlikSenseWebTicketURL(HttpServletRequest servletRequest, String vProxy, String hst, Users loggedInUser) {
        String webTicket = "";
        ServletContext servletContext = servletRequest.getServletContext();
        String path = servletContext.getRealPath("/WEB-INF/");
        String userdirectory = loggedInUser != null ? loggedInUser.getCompany().getCompanyAuthType() != null
                ? loggedInUser.getCompany().getCompanyAuthType().getUserDirectory() != null
                ? loggedInUser.getCompany().getCompanyAuthType().getUserDirectory()
                : ""
                : "" : "";
        String userid = loggedInUser != null
                ? loggedInUser.getCompany().getCompanyAuthType() != null
                ? loggedInUser.getCompany().getCompanyAuthType().getUserId() != null
                ? loggedInUser.getCompany().getCompanyAuthType().getUserId()
                : ""
                : ""
                : "";

        String username = loggedInUser != null
                ? loggedInUser.getCompany().getCompanyAuthType() != null
                ? loggedInUser.getCompany().getCompanyAuthType().getAuthType().getType().equals("OKTA")
                ? loggedInUser.getEmail()
                : loggedInUser.getAdUserName()
                : loggedInUser.getAdUserName()
                : "";

        webTicket = qlikViewManagement.getQlikSenseWebTicket(username, path, vProxy, hst, userdirectory, userid);
        logger.info("QlikSense web Ticket.........{}", webTicket);
        Map<String, String> ticketUrl = new HashMap<>();
        ticketUrl.put(ApplicationConstants.TICKET, webTicket);
        return ticketUrl;
    }
}
