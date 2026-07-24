package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.constants.LogEventTypeConstant;
import com.symphony.applaunch.dto.PaginationVO;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.IAppService;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.LoggingUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MstrStrategyTest {

    private UserService userService;
    private IAppService appService;
    private ClientUtil clientUtil;
    private LoggingUtility loggingUtility;
    private AppStrategyUtil appStrategyUtil;

    private MstrStrategy strategy;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        appService = mock(IAppService.class);
        clientUtil = mock(ClientUtil.class);
        loggingUtility = mock(LoggingUtility.class);
        appStrategyUtil = mock(AppStrategyUtil.class);

        strategy = new MstrStrategy(
                userService,
                appService,
                clientUtil,
                loggingUtility,
                appStrategyUtil
        );
    }

    @Test
    void execute_happyPath_shouldGenerateTokenSaveAndRedirect() throws Exception {
        // given: real entities so we can inspect fields
        Users user = new Users();
        user.setAdUserName("testUser"); // adjust if your field/setter name differs

        SHSApp app = new SHSApp();
        app.setId(1);
        app.setName("MicroStrategy App");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String redirectUrl = "http://mstr-host/report?someParam=1";

        // stubs used inside saveMicroStrategyTokenVerification
        when(clientUtil.getClientBrowser(request)).thenReturn("Chrome");
        when(userService.getUserFromToken(request)).thenReturn(user);
        when(appService.getAppById("1")).thenReturn(app);
        when(loggingUtility.getIpAddr(request)).thenReturn("127.0.0.1");

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then: tokenType is set
        assertEquals("MSTR", user.getTokenType());

        // token should be non-null of length 20
        String token = user.getToken();
        assertNotNull(token);
        assertEquals(20, token.length());

        // userService.saveRandomGeneratedToken called with same user and token
        verify(userService, times(1))
                .saveRandomGeneratedToken(user, token);

        // logging utility called once
        verify(clientUtil, times(1)).getClientBrowser(request);
        verify(userService, times(1)).getUserFromToken(request);
        verify(loggingUtility, times(1)).getIpAddr(request);
        verify(loggingUtility, times(1))
                .logUserEvent(
                        any(Users.class),
                        contains("MicroStrategy App" + LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG),
                        eq(LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER),
                        eq("Chrome"),
                        eq(1L),
                        isNull(),
                        eq("127.0.0.1")
                );

        // redirect URL should contain redirectUrl + "&token=" + token
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(urlCaptor.capture());
        String redirectedUrl = urlCaptor.getValue();

        assertTrue(redirectedUrl.startsWith(redirectUrl + "&token="));
        assertTrue(redirectedUrl.endsWith(token));
    }

    @Test
    void execute_whenSaveTokenThrows_shouldRedirectToErrorPage() throws RuntimeException, IOException {
        // given
        Users user = new Users();
        user.setAdUserName("testUser");

        SHSApp app = new SHSApp();
        app.setId(1);
        app.setName("MicroStrategy App");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String redirectUrl = "http://mstr-host/report?someParam=1";

        // spy the strategy to throw from saveMicroStrategyTokenVerification
        MstrStrategy spyStrategy = Mockito.spy(strategy);

        doThrow(new RuntimeException("boom"))
                .when(spyStrategy)
                .saveMicroStrategyTokenVerification(any(PaginationVO.class), eq(request));

        // when
        spyStrategy.execute(user, redirectUrl, app, request, response);

        // then: sendRedirect should NOT be called
        verify(response, never()).sendRedirect(anyString());

        // error redirect on appStrategyUtil invoked
        verify(appStrategyUtil, times(1))
                .redirectToErrorPage(
                        eq("0"),
                        contains("boom"),
                        eq(ApplicationConstants.MICROSTRATEGY),
                        eq(response)
                );
    }

    @Test
    void saveMicroStrategyTokenVerification_happyPath_shouldSaveTokenAndLog() throws Exception {
        // given
        Users user = new Users();
        user.setAdUserName("testUser");
        user.setToken("RANDOM_TOKEN_123");

        SHSApp app = new SHSApp();
        app.setId(1);
        app.setName("MicroStrategy App");

        PaginationVO paginationVO = new PaginationVO();
        paginationVO.setUser(user);
        paginationVO.setAppId("1");
        paginationVO.setAppname(app.getName());

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(clientUtil.getClientBrowser(request)).thenReturn("Firefox");
        when(userService.getUserFromToken(request)).thenReturn(user);
        when(appService.getAppById("1")).thenReturn(app);
        when(loggingUtility.getIpAddr(request)).thenReturn("10.0.0.1");

        // when
        strategy.saveMicroStrategyTokenVerification(paginationVO, request);

        // then: token saved
        verify(userService, times(1))
                .saveRandomGeneratedToken(user, "RANDOM_TOKEN_123");

        // logUserEvent invoked with correct data
        verify(loggingUtility, times(1))
                .logUserEvent(
                        eq(user),
                        contains("MicroStrategy App" + LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG + "testUser"),
                        eq(LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER),
                        eq("Firefox"),
                        eq(1L),
                        isNull(),
                        eq("10.0.0.1")
                );
    }

    @Test
    void saveMicroStrategyTokenVerification_whenLoggingFails_shouldThrowException() {
        // given
        Users user = new Users();
        user.setAdUserName("testUser");
        user.setToken("RANDOM_TOKEN_123");

        SHSApp app = new SHSApp();
        app.setId(1);
        app.setName("MicroStrategy App");

        PaginationVO paginationVO = new PaginationVO();
        paginationVO.setUser(user);
        paginationVO.setAppId("1");
        paginationVO.setAppname(app.getName());

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(clientUtil.getClientBrowser(request)).thenReturn("Edge");
        when(userService.getUserFromToken(request)).thenReturn(user);
        when(appService.getAppById("1")).thenReturn(app);
        when(loggingUtility.getIpAddr(request)).thenReturn("192.168.1.100");

        // force an exception from logUserEvent
        doThrow(new RuntimeException("log failed"))
                .when(loggingUtility)
                .logUserEvent(
                        any(Users.class),
                        anyString(),
                        anyInt(),
                        anyString(),
                        anyLong(),
                        any(),
                        anyString()
                );

        // when + then: wrapper Exception is thrown
        Exception ex = assertThrows(Exception.class,
                () -> strategy.saveMicroStrategyTokenVerification(paginationVO, request));

        assertTrue(ex.getMessage().contains("log failed"));

        // still should have saved the token before the failure
        verify(userService, times(1))
                .saveRandomGeneratedToken(user, "RANDOM_TOKEN_123");
    }
}
