package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.util.QlikViewManagement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for QlikViewStrategy.
 * No Spring context, using Mockito.mock(...) directly.
 */
class QlikViewStrategyTest {

    private QlikViewManagement qlikViewManagement;
    private QlikViewStrategy strategy;

    @BeforeEach
    void setUp() {
        qlikViewManagement = mock(QlikViewManagement.class);
        strategy = new QlikViewStrategy(qlikViewManagement);
    }

    @Test
    void execute_withAccessPointUrl_shouldGenerateTicketAndRedirect() throws Exception {
        // given
        Users loggedInUser = mock(Users.class); // not used in this method, but passed for signature completeness
        when(loggedInUser.getAdUserName()).thenReturn("testUser");
        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getAttribute(ApplicationConstants.LOGGED_IN_USER)).thenReturn(loggedInUser);

        // A redirect URL that triggers the "AccessPoint" + "%7C" branch
        String redirectUrl = "https://somehost/AccessPoint/something%7CmyDoc.qvw&param=1";

        // getWebTicketAndValidate uses request attribute LOGGED_IN_USER (only in that method),
        // but execute(...) calls getWebTicketAndValidate directly, so we don't need it here.
        when(qlikViewManagement.getQlikViewWebTicket(anyString(), anyString(), anyString()))
                .thenReturn("https://ticket-url/with-token");

        // when
        strategy.execute(loggedInUser, redirectUrl, app, request, response);

        // then: internal helper should have been used to get ticket URL,
        // and sendRedirect() should be called with that URL.
        verify(qlikViewManagement, times(1))
                .getQlikViewWebTicket(anyString(), anyString(), anyString());

        verify(response, times(1))
                .sendRedirect("https://ticket-url/with-token");
    }

    @Test
    void execute_whenSendRedirectThrowsIOException_shouldNotPropagateException() throws Exception {
        // given
        Users loggedInUser = mock(Users.class);
        when(loggedInUser.getAdUserName()).thenReturn("testUser");
        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getAttribute(ApplicationConstants.LOGGED_IN_USER)).thenReturn(loggedInUser);

        String redirectUrl = "https://somehost/AccessPoint/something%7Cdoc.qvw&param=1";

        when(qlikViewManagement.getQlikViewWebTicket(anyString(), anyString(), anyString()))
                .thenReturn("https://ticket-url/with-token");

        // Make sendRedirect throw IOException to hit the catch block
        doThrow(new IOException("boom")).when(response).sendRedirect(anyString());

        // when / then: no exception should escape from execute(...)
        assertDoesNotThrow(() ->
                strategy.execute(loggedInUser, redirectUrl, app, request, response)
        );

        verify(qlikViewManagement, times(1))
                .getQlikViewWebTicket(anyString(), anyString(), anyString());

        verify(response, times(1))
                .sendRedirect("https://ticket-url/with-token");
    }

    @Test
    void execute_whenGetWebTicketReturnsNullBody_shouldRedirectWithEmptyString() throws Exception {
        // given
        Users loggedInUser = mock(Users.class);
        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String redirectUrl = "https://somehost/AccessPoint/something%7Cdoc.qvw&param=1";

        // Use an anonymous subclass to override getWebTicketAndValidate and return null body
        QlikViewStrategy strategyWithNullBody = new QlikViewStrategy(qlikViewManagement) {
            @Override
            public ResponseEntity<Map<String, String>> getWebTicketAndValidate(HttpServletRequest req,
                                                                               String document,
                                                                               String host) {
                return ResponseEntity.ok(null); // body is null
            }
        };

        // when
        strategyWithNullBody.execute(loggedInUser, redirectUrl, app, request, response);

        // then: ticketUrl becomes "", so sendRedirect("") should be called
        verify(response, times(1)).sendRedirect("");
    }

    @Test
    void execute_withDocumentUrl_shouldStillGenerateTicketAndRedirect() throws Exception {
        // given
        Users loggedInUser = mock(Users.class);
        when(loggedInUser.getAdUserName()).thenReturn("testUser");
        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getAttribute(ApplicationConstants.LOGGED_IN_USER)).thenReturn(loggedInUser);

        // URL designed to hit the "document" branch (exact parsing depends on ApplicationConstants,
        // but we only care that we call qlikViewManagement and redirect)
        String redirectUrl = "https://somehost/qv?document=myDoc.qvw&param=1";

        when(qlikViewManagement.getQlikViewWebTicket(anyString(), anyString(), anyString()))
                .thenReturn("https://ticket-url-doc-branch");


        // when
        strategy.execute(loggedInUser, redirectUrl, app, request, response);

        // then
        verify(qlikViewManagement, times(1))
                .getQlikViewWebTicket(anyString(), anyString(), anyString());

        verify(response, times(1))
                .sendRedirect("https://ticket-url-doc-branch");
    }

    @Test
    void getWebTicketAndValidate_shouldCallQlikViewManagementAndReturnTicketInResponseEntity() {
        // given
        Users loggedInUser = mock(Users.class);
        when(loggedInUser.getAdUserName()).thenReturn("testUser");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // request attribute is used to read logged-in user
        when(request.getAttribute(ApplicationConstants.LOGGED_IN_USER)).thenReturn(loggedInUser);

        String document = "myDoc.qvw;host=somehost";
        String host = "somehost";

        when(qlikViewManagement.getQlikViewWebTicket("testUser", document, host))
                .thenReturn("https://ticket-url-from-helper");

        // when
        ResponseEntity<Map<String, String>> result =
                strategy.getWebTicketAndValidate(request, document, host);

        // then
        verify(qlikViewManagement, times(1))
                .getQlikViewWebTicket("testUser", document, host);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals("https://ticket-url-from-helper",
                result.getBody().get(ApplicationConstants.TICKET_URL));
    }
}
