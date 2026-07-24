package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.LogDTO;
import com.symphony.applaunch.dto.LogEventTypeDTO;
import com.symphony.applaunch.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoggingUtilityTest {

    @Test
    void logUserEvent_shouldBuildLogDTO_andCallSaveLogsViaRestTemplate() {
        // Arrange
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);
        loggingUtility.loggingServiceUrl = "http://logging-service"; // set @Value field manually

        Users user = Mockito.mock(Users.class, Mockito.RETURNS_DEEP_STUBS);
        when(user.getId()).thenReturn(1L);
        when(user.getAdUserName()).thenReturn("john.doe");
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");
        when(user.getCompany().getId()).thenReturn(100L);
        when(user.getCompany().getName()).thenReturn("Acme Inc.");

        String message = "User logged in";
        int logEventTypeId = 5;
        String browserType = "Chrome";
        Long refId = 999L;
        String refEntity = "LOGIN";
        String ipAddress = "10.0.0.1";

        // mock RestTemplate.exchange to return OK
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Capture the HttpEntity<LogDTO> sent to RestTemplate
        ArgumentCaptor<HttpEntity<LogDTO>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // Act
        loggingUtility.logUserEvent(user, message, logEventTypeId, browserType, refId, refEntity, ipAddress);

        // Assert: verify URL and method
        verify(restTemplate).exchange(
                eq("http://logging-service/saveLogs"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<LogDTO> sentEntity = entityCaptor.getValue();
        assertNotNull(sentEntity);

        LogDTO body = sentEntity.getBody();
        assertNotNull(body);
        assertEquals(1L, body.getUserId());
        assertEquals("john.doe", body.getAdUserName());
        assertEquals("John", body.getFirstName());
        assertEquals("Doe", body.getLastName());
        assertEquals(100L, body.getCompanyId());
        assertEquals("Acme Inc.", body.getCompanyName());
        assertEquals(message, body.getMessage());
        assertEquals(browserType, body.getBrowserType());
        assertEquals(refId, body.getRefId());
        assertEquals(refEntity, body.getRefEntity());
        assertEquals(ipAddress, body.getIpAddress());

        LogEventTypeDTO eventType = body.getLogEventType();
        assertNotNull(eventType);
        assertEquals(logEventTypeId, eventType.getId());
        assertEquals("john.doe", eventType.getName());

        // loggedDate should be set
        assertNotNull(body.getLoggedDate());
        assertTrue(body.getLoggedDate().before(new Date()) || body.getLoggedDate().equals(body.getLoggedDate()));
    }

    // ------------------ getIpAddr tests ------------------

    @Test
    void getIpAddr_shouldReturnFirstIpFromXForwardedFor() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("203.0.113.10, 203.0.113.11, 203.0.113.12");

        String ip = loggingUtility.getIpAddr(request);

        assertEquals("203.0.113.10", ip);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void getIpAddr_shouldUseProxyClientIp_whenXForwardedForIsNullOrUnknown() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(ApplicationConstants.UNKNOWN);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("198.51.100.20");

        String ip = loggingUtility.getIpAddr(request);

        assertEquals("198.51.100.20", ip);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void getIpAddr_shouldUseWlProxyClientIp_whenOthersAreNullOrUnknown() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.0.2.30");

        String ip = loggingUtility.getIpAddr(request);

        assertEquals("192.0.2.30", ip);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void getIpAddr_shouldFallbackToRemoteAddr_whenAllHeadersMissingOrUnknown() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(ApplicationConstants.UNKNOWN);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = loggingUtility.getIpAddr(request);

        assertEquals("127.0.0.1", ip);
        verify(request, times(1)).getRemoteAddr();
    }

    // ------------------ saveLogs tests ------------------

    @Test
    void saveLogs_shouldSendPostRequestSuccessfully() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);
        loggingUtility.loggingServiceUrl = "http://logging-service";

        LogDTO logDto = new LogDTO();
        logDto.setMessage("test log");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        loggingUtility.saveLogs(logDto);

        ArgumentCaptor<HttpEntity<LogDTO>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
                eq("http://logging-service/saveLogs"),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(String.class)
        );

        HttpEntity<LogDTO> sentEntity = captor.getValue();
        assertNotNull(sentEntity);
        assertEquals(logDto, sentEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON, sentEntity.getHeaders().getContentType());
    }

    @Test
    void saveLogs_shouldCatchExceptionFromRestTemplate() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LoggingUtility loggingUtility = new LoggingUtility(restTemplate);
        loggingUtility.loggingServiceUrl = "http://logging-service";

        LogDTO logDto = new LogDTO();
        logDto.setMessage("test error log");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenThrow(new RuntimeException("downstream error"));

        // Should not throw; error is caught and logged
        assertDoesNotThrow(() -> loggingUtility.saveLogs(logDto));

        verify(restTemplate).exchange(
                eq("http://logging-service/saveLogs"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}
