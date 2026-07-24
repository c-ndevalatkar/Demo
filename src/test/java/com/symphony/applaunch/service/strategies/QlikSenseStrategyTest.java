package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.util.QlikViewManagement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QlikSenseStrategyTest {

    private QlikViewManagement qlikViewManagement;
    private QlikSenseStrategy strategy;

    @BeforeEach
    void setUp() {
        qlikViewManagement = mock(QlikViewManagement.class);
        strategy = new QlikSenseStrategy(qlikViewManagement);
    }

    @Test
    void execute_oktaUser_shouldUseEmailAndRedirectWithTicket() throws Exception {
        // given
        Users user = mock(Users.class, RETURNS_DEEP_STUBS);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getAdUserName()).thenReturn("adUser123");
        // company/companyAuthType nested values
        when(user.getCompany().getCompanyAuthType().getUserDirectory()).thenReturn("DIR1");
        when(user.getCompany().getCompanyAuthType().getUserId()).thenReturn("userId123");
        when(user.getCompany().getCompanyAuthType().getAuthType().getType()).thenReturn("OKTA");

        SHSApp app = mock(SHSApp.class);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/WEB-INF/")).thenReturn("/some/path/WEB-INF");

        String redirectUrl = "https://myhost.example.com/vproxy/some/path";

        // Qlik web ticket
        when(qlikViewManagement.getQlikSenseWebTicket(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ticket123");

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then
        // verify QlikViewManagement was called with correct arguments derived from user + URL
        String expectedHost = redirectUrl.toLowerCase().split("://")[1].split("/")[0];
        String expectedVProxy = redirectUrl.split(ApplicationConstants.DOT_COM)[1].split("/")[0]; // may be "" for `/vproxy/...`

        verify(qlikViewManagement, times(1))
                .getQlikSenseWebTicket(
                        eq("user@example.com"),         // OKTA => email
                        eq("/some/path/WEB-INF"),
                        eq(expectedVProxy),
                        eq(expectedHost),
                        eq("DIR1"),
                        eq("userId123")
                );

        // capture redirect URL
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String actualRedirect = redirectCaptor.getValue();
        // We don’t depend on the exact constant value, but we know the pattern
        // redirectUrl + QS_TICKET_RD_URL + ticket
        String expectedPrefix = redirectUrl + ApplicationConstants.QS_TICKET_RD_URL;
        assertEquals(expectedPrefix + "ticket123", actualRedirect);
    }

    @Test
    void execute_nonOktaUser_shouldUseAdUserName() throws Exception {
        // given
        Users user = mock(Users.class, RETURNS_DEEP_STUBS);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getAdUserName()).thenReturn("adUser123");
        when(user.getCompany().getCompanyAuthType().getUserDirectory()).thenReturn("DIR2");
        when(user.getCompany().getCompanyAuthType().getUserId()).thenReturn("userId999");
        // Auth type not OKTA -> should use adUserName
        when(user.getCompany().getCompanyAuthType().getAuthType().getType()).thenReturn("SAML");

        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/WEB-INF/")).thenReturn("/path/web-inf");

        String redirectUrl = "https://anotherhost.example.com/someproxy/path";

        when(qlikViewManagement.getQlikSenseWebTicket(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ticketXYZ");

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then
        String expectedHost = redirectUrl.toLowerCase().split("://")[1].split("/")[0];
        String expectedVProxy = redirectUrl.split(ApplicationConstants.DOT_COM)[1].split("/")[0];

        // username should be AD user instead of email
        verify(qlikViewManagement).getQlikSenseWebTicket(
                eq("adUser123"),
                eq("/path/web-inf"),
                eq(expectedVProxy),
                eq(expectedHost),
                eq("DIR2"),
                eq("userId999")
        );

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String actualRedirect = redirectCaptor.getValue();
        String expectedPrefix = redirectUrl + ApplicationConstants.QS_TICKET_RD_URL;
        assertEquals(expectedPrefix + "ticketXYZ", actualRedirect);
    }

    @Test
    void execute_whenLoggedInUserIsNull_shouldFallbackToEmptyUserAndStillRedirect() throws Exception {
        // given
        Users user = null;
        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/WEB-INF/")).thenReturn("/root/web-inf");

        String redirectUrl = "https://host.example.com/proxy/path";

        when(qlikViewManagement.getQlikSenseWebTicket(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ticketNULL");

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then
        // username, directory and userId should all effectively be "", but we stubbed with anyString, so we only assert redirect
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String actualRedirect = redirectCaptor.getValue();
        String expectedPrefix = redirectUrl + ApplicationConstants.QS_TICKET_RD_URL;
        assertEquals(expectedPrefix + "ticketNULL", actualRedirect);
    }

    @Test
    void execute_whenSendRedirectThrowsIOException_shouldNotPropagateException() throws Exception {
        // given
        Users user = mock(Users.class, RETURNS_DEEP_STUBS);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getAdUserName()).thenReturn("adUser123");
        when(user.getCompany().getCompanyAuthType().getUserDirectory()).thenReturn("DIR3");
        when(user.getCompany().getCompanyAuthType().getUserId()).thenReturn("userId3");
        when(user.getCompany().getCompanyAuthType().getAuthType().getType()).thenReturn("OKTA");

        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath("/src/main/webapp/WEB-INF/")).thenReturn("/app/WEB-INF");

        String redirectUrl = "https://host2.example.com/x/y";

        when(qlikViewManagement.getQlikSenseWebTicket(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ticketIO");

        // make sendRedirect throw IOException to exercise catch block
        doThrow(new IOException("boom")).when(response)
                .sendRedirect(anyString());

        // when / then: just ensure no exception propagates
        strategy.execute(user, redirectUrl, app, request, response);

        // verify we still tried to redirect once
        verify(response, times(1)).sendRedirect(anyString());
    }
}
