package com.symphony.applaunch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.MSTRAuthResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class MstrAuthServiceTest {

    @InjectMocks
    private MstrAuthService mstrService;

    @Mock
    private Environment environment;

    @Mock
    private CloseableHttpClient httpClient;

    @Before
    public void setUp() {
        // Inject config fields used in loginAndVerify
        ReflectionTestUtils.setField(mstrService, "mstrApiLoginPath", "/api/auth/login");
        ReflectionTestUtils.setField(mstrService, "mstrApiLogoutPath", "/api/auth/logout");
        ReflectionTestUtils.setField(mstrService, "mstrLoginMode", 1);
        ReflectionTestUtils.setField(mstrService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(mstrService, "environment", environment);
    }

    // ---------------------------------------------------------
    // loginAndVerify(..) tests
    // ---------------------------------------------------------

    @Test
    public void loginAndVerify_whenLoginReturns401_shouldFailWithAuthMessage() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        String rawUrl = "https://mstr.example.com/MicroStrategy/servlet/mstrWeb"
                + "?evt=1234&src=main"
                + "&Server=myServer"
                + "&Project=Account+Metrics"
                + "&Port=0";

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(rawUrl);

        CloseableHttpResponse loginResp = mock(CloseableHttpResponse.class);
        when(loginResp.getCode()).thenReturn(401);

        when(httpClient.execute(
                any(HttpPost.class),
                any(org.apache.hc.client5.http.protocol.HttpClientContext.class)
        )).thenReturn(loginResp);

        MSTRAuthResult result = mstrService.loginAndVerify(rawUrl,"user", "bad-pass", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getRawStatus().contains("Authentication failed"));
    }

    @Test
    public void loginAndVerify_whenLoginReturns500_shouldFailWithLoginMessage() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        String rawUrl = "https://mstr.example.com/MicroStrategy/servlet/mstrWeb"
                + "?evt=1234&src=main"
                + "&Server=myServer"
                + "&Project=Account+Metrics"
                + "&Port=0";

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(rawUrl);

        CloseableHttpResponse loginResp = mock(CloseableHttpResponse.class);
        when(loginResp.getCode()).thenReturn(500);

        when(httpClient.execute(
                any(HttpPost.class),
                any(org.apache.hc.client5.http.protocol.HttpClientContext.class)
        )).thenReturn(loginResp);

        MSTRAuthResult result = mstrService.loginAndVerify(rawUrl, "user", "pass", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getRawStatus().contains("Login failed: 500"));
    }

    @Test
    public void loginAndVerify_whenLogin204AndVerify200_shouldReturnSuccessAndVerifiedUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        String rawUrl = "https://mstr.example.com/MicroStrategy/servlet/mstrWeb"
                + "?evt=1234&src=main"
                + "&Server=myServer"
                + "&Project=Account+Metrics"
                + "&Port=0";

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(rawUrl);

        CloseableHttpResponse loginResp = mock(CloseableHttpResponse.class);
        CloseableHttpResponse verifyResp = mock(CloseableHttpResponse.class);
        CloseableHttpResponse logoutResp = mock(CloseableHttpResponse.class);

        // login
        when(loginResp.getCode()).thenReturn(204);
        Header tokenHeader = mock(Header.class);
        when(tokenHeader.getValue()).thenReturn("AUTH-TOKEN-123");
        when(loginResp.getFirstHeader("X-MSTR-AuthToken")).thenReturn(tokenHeader);

        // logout
        when(logoutResp.getCode()).thenReturn(204);

        when(httpClient.execute(
                any(HttpPost.class),
                any(org.apache.hc.client5.http.protocol.HttpClientContext.class)
        )).thenReturn(loginResp);

        when(httpClient.execute(
                any(HttpGet.class),
                any(org.apache.hc.client5.http.protocol.HttpClientContext.class)
        )).thenReturn(verifyResp);

        MSTRAuthResult result = mstrService.loginAndVerify(rawUrl, "user", "pass", false);

        assertTrue(result.isSuccess());
        assertEquals("AUTH-TOKEN-123", result.getMstrAuthToken());
    }

    @Test
    public void loginAndVerify_whenExceptionThrown_shouldReturnFailureWithExceptionMessage() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        String rawUrl = "https://mstr.example.com/MicroStrategy/servlet/mstrWeb"
                + "?evt=1234&src=main"
                + "&Server=myServer"
                + "&Project=Account+Metrics"
                + "&Port=0";

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(rawUrl);
        when(httpClient.execute(
                any(HttpPost.class),
                any(org.apache.hc.client5.http.protocol.HttpClientContext.class)
        )).thenThrow(new RuntimeException("boom"));

        MSTRAuthResult result = mstrService.loginAndVerify(rawUrl, "user", "pass", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getRawStatus().contains("boom"));
    }

    // ---------------------------------------------------------
    // loginAndLaunch(..) tests
    // ---------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void loginAndLaunch_whenRedirectUrlMissing_shouldThrowIllegalArgumentException() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(null);

        mstrService.loginAndLaunch("user", "pass", request, response);
    }

    @Test
    public void loginAndLaunch_happyPath_shouldWriteHtmlFormAndSubmitScript() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String rawUrl = "https://mstr.example.com/MicroStrategy/servlet/mstrWeb"
                + "?evt=1234&src=main"
                + "&Server=myServer"
                + "&Project=Account+Metrics"
                + "&Port=0";

        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(rawUrl);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        mstrService.loginAndLaunch("myUser", "myPass", request, response);

        pw.flush();
        String html = sw.toString();

        // Basic sanity checks on generated HTML
        assertTrue(html.contains("form id='mstrForm'"));
        assertTrue(html.contains("method='post'"));
        assertTrue(html.contains("action='https://mstr.example.com/MicroStrategy/servlet/mstrWeb'"));

        // values should be HTML-escaped and project '+' -> space
        assertTrue(html.contains("name='Uid' value='myUser'"));
        assertTrue(html.contains("name='Pwd' value='myPass'"));
        assertTrue(html.contains("name='Project' value='Account Metrics'"));
        assertTrue(html.contains("name='Server' value='myServer'"));
        assertTrue(html.contains("name='Port' value='0'"));
        assertTrue(html.contains("name='evt' type='hidden' class='mstrHiddenInput' value='1234'"));
        assertTrue(html.contains("name='src' type='hidden' class='mstrHiddenInput' value='main'"));

        verify(response).setContentType(MediaType.TEXT_HTML_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
    }

    // ---------------------------------------------------------
    // redirectToErrorPage(..) tests
    // ---------------------------------------------------------

    @Test
    public void redirectToErrorPage_shouldBuildContextRelativeUrlWithParams() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/app");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://host/app/some/path"));
        when(request.getServerPort()).thenReturn(443);

        when(environment.getProperty(ApplicationConstants.SERVER)).thenReturn("my-server");

        mstrService.redirectToErrorPage(
                request,
                response,
                "E001",
                "Something bad happened",
                "MyProject"
        );

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();

        // Something like: /app/error/mstr-error?ErrorCode=E001&ErrorMessage=Something+bad+happened&Project=MyProject&...
        log.info("Redirect url in edirectToErrorPage_shouldBuildContextRelativeUrlWithParams() : {}", redirectUrl);
        assertTrue(redirectUrl.startsWith("/app/error/mstr-error"));
        assertTrue(redirectUrl.contains("ErrorCode=E001"));
        assertTrue(redirectUrl.contains("ErrorMessage=Something%20bad%20happened"));
        assertTrue(redirectUrl.contains("Project=MyProject"));
        assertTrue(redirectUrl.contains("OriginalURL=https://host/app/some/path"));
        assertTrue(redirectUrl.contains("Server=https://my-server"));
        assertTrue(redirectUrl.contains("Port=443"));
    }

    @Test
    public void redirectToErrorPage_withNulls_shouldFallbackToDefaults() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://host/path"));
        when(request.getServerPort()).thenReturn(8080);
        when(environment.getProperty(ApplicationConstants.SERVER)).thenReturn(null);

        mstrService.redirectToErrorPage(
                request,
                response,
                null,
                null,
                null
        );

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();

        // Defaults: code "", msg "Unknown error", proj ""
        assertTrue(redirectUrl.startsWith("/error/mstr-error"));
        assertTrue(redirectUrl.contains("ErrorCode="));
        log.info("Redirect url : {}", redirectUrl);
        assertTrue(redirectUrl.contains("ErrorMessage=Unknown%20error"));
        assertTrue(redirectUrl.contains("Project="));
        assertTrue(redirectUrl.contains("Port=8080"));
    }
}