package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.LogDTO;
import com.symphony.applaunch.dto.LogEventTypeDTO;
import com.symphony.applaunch.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Component
@Slf4j
public class LoggingUtility {

    private RestTemplate restTemplate;

    @Value("${logging.service.url}")
    public String loggingServiceUrl;

    public LoggingUtility(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This is a common method that can used to save application logs to logging microservice
     */

    public void logUserEvent(Users userObject, String message, int logEventTypeId, String broweserType, Long refId,
                             String refEntity, String ipAddress) {

        log.info("LoggingUtility :: logUserEvent() called!");
        LogEventTypeDTO logEventTypeDto = new LogEventTypeDTO();
        logEventTypeDto.setId(logEventTypeId);
        logEventTypeDto.setName(userObject.getAdUserName());
        LogDTO log = new LogDTO();
        log.setUserId(userObject.getId());
        log.setAdUserName(userObject.getAdUserName());
        log.setFirstName(userObject.getFirstName());
        log.setLastName(userObject.getLastName());
        log.setLogEventType(logEventTypeDto);
        log.setMessage(message);
        log.setBrowserType(broweserType);
        log.setLoggedDate(new Date());
        log.setCompanyId(userObject.getCompany().getId());
        log.setRefId(refId);
        log.setRefEntity(refEntity);
        log.setIpAddress(ipAddress);
        log.setCompanyName(userObject.getCompany().getName());
        this.saveLogs(log);
    }

    /**
     * This method is for getting an IPAddress
     */

    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !ApplicationConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            // X-Forwarded-For can contain multiple IPs, first one is the client's IP
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !ApplicationConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !ApplicationConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * Asynchronously sends log data to the external logging service.
     * This method builds an HTTP POST request with the provided log data and sends
     * it to the configured logging service endpoint. It is marked as
     * {@code @Async}, so it executes in a separate thread and does not block the
     * caller.
     *
     * @param logObj the log data to be sent to the logging service
     */
    @Async
    public void saveLogs(LogDTO logObj) {
        log.info("LoggingUtility saveLogs:: called with request body - {}", logObj);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LogDTO> entity = new HttpEntity<>(logObj, headers);

            restTemplate.exchange(loggingServiceUrl + "/saveLogs", HttpMethod.POST, entity, String.class);
            log.info("LoggingUtility saveLogs:: log sent successfully.");
        } catch (Exception e) {
            log.error("LoggingUtility saveLogs:: error while sending log", e);
        }
    }
}
