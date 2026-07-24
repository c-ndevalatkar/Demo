package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MdtStrategyTest {

    @Mock
    private AppStrategyUtil appStrategyUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Users loggedInUser;

    @Mock
    private SHSApp app;

    @InjectMocks
    private MdtStrategy mdtStrategy;

    @Test
    void execute_shouldSetUserFieldsAndRedirect_whenTicketPresent() throws Exception {
        String redirectUrl = "https://example.com/app";
        String appId = "MY_APP";
        String userId = "123";
        String ticketValue = "ticket-123";

        // request params
        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userId);

        // generateWebTicket returns body with ticket
        Map<String, String> body = new HashMap<>();
        body.put(ApplicationConstants.TICKET, ticketValue);
        when(appStrategyUtil.generateWebTicket(loggedInUser, request))
                .thenReturn(ResponseEntity.ok(body));

        // call
        mdtStrategy.execute(loggedInUser, redirectUrl, app, request, response);

        // verify user fields set
        verify(loggedInUser).setTokenType("MDT");
        verify(loggedInUser).setSsoAppId(appId);

        // verify redirect URL (redirectUrl + TOKEN_LOG_INFO + ticketValue)
        String expectedRedirect = redirectUrl + ApplicationConstants.TOKEN_LOG_INFO + ticketValue;
        verify(response).sendRedirect(expectedRedirect);

        // ensure no error redirect
        verify(appStrategyUtil, never()).redirectToErrorPage(anyString(), anyString(), anyString(), any());
    }

    @Test
    void execute_shouldRedirectWithEmptyTicket_whenResponseBodyIsNull() throws Exception {
        String redirectUrl = "https://example.com/app";
        String appId = "MY_APP";
        String userId = "456";

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userId);

        // generateWebTicket returns null body
        when(appStrategyUtil.generateWebTicket(loggedInUser, request))
                .thenReturn(ResponseEntity.ok(null));

        mdtStrategy.execute(loggedInUser, redirectUrl, app, request, response);

        // user fields still set
        verify(loggedInUser).setTokenType("MDT");
        verify(loggedInUser).setSsoAppId(appId);

        // ticketUrl should be "" -> redirectUrl + TOKEN_LOG_INFO + ""
        String expectedRedirect = redirectUrl + ApplicationConstants.TOKEN_LOG_INFO + "";
        verify(response).sendRedirect(expectedRedirect);

        verify(appStrategyUtil, never()).redirectToErrorPage(anyString(), anyString(), anyString(), any());
    }

    @Test
    void execute_shouldRedirectToErrorPage_whenSendRedirectThrowsIOException() throws Exception {
        String redirectUrl = "https://example.com/app";
        String appId = "MY_APP";
        String userId = "789";
        String ticketValue = "ticket-err";

        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);
        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userId);

        Map<String, String> body = new HashMap<>();
        body.put(ApplicationConstants.TICKET, ticketValue);
        when(appStrategyUtil.generateWebTicket(loggedInUser, request))
                .thenReturn(ResponseEntity.ok(body));

        // sendRedirect throws IOException
        doThrow(new IOException("IO error"))
                .when(response).sendRedirect(anyString());

        mdtStrategy.execute(loggedInUser, redirectUrl, app, request, response);

        // sendRedirect was attempted
        verify(response).sendRedirect(
                redirectUrl + ApplicationConstants.TOKEN_LOG_INFO + ticketValue
        );

        // error path must be invoked with MDT
        verify(appStrategyUtil).redirectToErrorPage(
                eq("0"),
                eq("IO error"),
                eq(ApplicationConstants.MDT),
                eq(response)
        );
    }
}