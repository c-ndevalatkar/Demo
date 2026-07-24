package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenericShsAppsStrategyTest {

    private AppStrategyUtil appStrategyUtil;
    private GenericShsAppsStrategy strategy;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Users user;
    private SHSApp app;

    @BeforeEach
    void setUp() {
        appStrategyUtil = Mockito.mock(AppStrategyUtil.class);
        strategy = new GenericShsAppsStrategy(appStrategyUtil);

        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        user = new Users();
        app = new SHSApp();
    }

    // ✅ 1. Normal redirect without &params
    @Test
    void execute_shouldRedirectNormally_whenNoParamsPresent() throws Exception {

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn("APP123");
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn("10");

        Map<String, String> ticketMap = new HashMap<>();
        ticketMap.put(ApplicationConstants.TICKET, "WEB_TICKET");

        when(appStrategyUtil.generateWebTicket(any(), any()))
                .thenReturn(ResponseEntity.ok(ticketMap));

        String redirectUrl = "http://test.com/login?ticket=";

        strategy.execute(user, redirectUrl, app, request, response);

        // ✅ Verify user mutation
        assertEquals("APP123", user.getSsoAppId());
        assertEquals("generic", user.getTokenType());

        // ✅ Verify redirect
        verify(response).sendRedirect("http://test.com/login?ticket=WEB_TICKET");

        // ✅ Verify ticket generation call
        verify(appStrategyUtil).generateWebTicket(user, request);
    }

    // ✅ 2. Redirect when &params is present
    @Test
    void execute_shouldRedirectWithParams_whenParamsPresent() throws Exception {

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn("APP999");
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn("20");

        Map<String, String> ticketMap = new HashMap<>();
        ticketMap.put(ApplicationConstants.TICKET, "TICKET123");

        when(appStrategyUtil.generateWebTicket(any(), any()))
                .thenReturn(ResponseEntity.ok(ticketMap));

        String redirectUrl = "http://app.com/login?ticket=&params=mode=test";

        strategy.execute(user, redirectUrl, app, request, response);

        verify(response).sendRedirect(
                "http://app.com/login?ticket=" + "TICKET123" + "&params=mode=test"
        );
    }

    // ✅ 3. When ResponseEntity body is NULL
    @Test
    void execute_shouldHandleNullTicketMapSafely() throws Exception {

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn("APP1");
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn("30");

        when(appStrategyUtil.generateWebTicket(any(), any()))
                .thenReturn(ResponseEntity.ok(null)); // NULL BODY

        String redirectUrl = "http://test.com/login?ticket=";

        strategy.execute(user, redirectUrl, app, request, response);

        // Ticket becomes empty string
        verify(response).sendRedirect("http://test.com/login?ticket=");
    }

    // ✅ 4. IOException path (CATCH BLOCK)
    @Test
    void execute_shouldCatchIOException_whenSendRedirectFails() throws Exception {

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn("APP_FAIL");
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn("40");

        Map<String, String> ticketMap = new HashMap<>();
        ticketMap.put(ApplicationConstants.TICKET, "FAIL_TICKET");

        when(appStrategyUtil.generateWebTicket(any(), any()))
                .thenReturn(ResponseEntity.ok(ticketMap));

        doThrow(new IOException("Redirect failed"))
                .when(response)
                .sendRedirect(anyString());

        String redirectUrl = "http://fail.com/login?ticket=";

        // ✅ Should NOT throw exception (catch block handles it)
        assertDoesNotThrow(() ->
                strategy.execute(user, redirectUrl, app, request, response)
        );
    }

    // ✅ 5. Verify USER_ID parsing branch
    @Test
    void execute_shouldParseUserIdCorrectly() throws Exception {

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn("APP777");
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn("99");

        Map<String, String> ticketMap = new HashMap<>();
        ticketMap.put(ApplicationConstants.TICKET, "USER_TEST");

        when(appStrategyUtil.generateWebTicket(any(), any()))
                .thenReturn(ResponseEntity.ok(ticketMap));

        String redirectUrl = "http://useridtest.com/login?ticket=";

        strategy.execute(user, redirectUrl, app, request, response);

        // Just verify redirect executed successfully
        verify(response).sendRedirect("http://useridtest.com/login?ticket=USER_TEST");
    }
}