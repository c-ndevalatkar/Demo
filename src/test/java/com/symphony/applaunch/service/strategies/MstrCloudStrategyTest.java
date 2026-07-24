package com.symphony.applaunch.service.strategies;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.service.IAppService;
import com.symphony.applaunch.service.MstrAuthService;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.LoggingUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MstrCloudStrategyTest {

    private UserService userService;
    private IAppService appService;
    private ClientUtil clientUtil;
    private LoggingUtility loggingUtility;
    private MstrAuthService mstrAuthService;
    private AppStrategyUtil appStrategyUtil;

    private MstrCloudStrategy strategy;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        appService = mock(IAppService.class);
        clientUtil = mock(ClientUtil.class);
        loggingUtility = mock(LoggingUtility.class);
        mstrAuthService = mock(MstrAuthService.class);
        appStrategyUtil = mock(AppStrategyUtil.class);

        strategy = new MstrCloudStrategy(
                userService,
                appService,
                clientUtil,
                loggingUtility,
                mstrAuthService,
                appStrategyUtil
        );

        // inject property value manually
        ReflectionTestUtils.setField(strategy, "mstrUserLoginPassword", "TestPWD123");
    }

    @Test
    void execute_whenLoginAndLaunchSucceeds_shouldCallLoginAndLaunchWithLowercaseUsername() throws Exception {
        // given
        Users user = mock(Users.class);
        // deliberately mixed-case to prove the toLowerCase() conversion is exercised
        when(user.getAdUserName()).thenReturn("TestUser");

        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String redirectUrl = "http://example.com/mstr";

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then
        verify(mstrAuthService, times(1))
                .loginAndLaunch("testuser", "TestPWD123", request, response);

        verifyNoInteractions(appStrategyUtil);
    }

    @Test
    void execute_whenLoginAndLaunchThrowsException_shouldRedirectViaAppStrategyUtil() throws Exception {
        // given
        Users user = mock(Users.class);
        when(user.getAdUserName()).thenReturn("testuser");

        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String redirectUrl = "http://example.com/mstr";

        doThrow(new RuntimeException("boom"))
                .when(mstrAuthService)
                .loginAndLaunch(anyString(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then: loginAndLaunch was attempted once (and threw)
        verify(mstrAuthService, times(1))
                .loginAndLaunch("testuser", "TestPWD123", request, response);

        // catch-block error redirect via AppStrategyUtil MUST be called
        verify(appStrategyUtil, times(1))
                .redirectToErrorPage(
                        eq("0"),
                        contains("boom"),
                        eq(ApplicationConstants.MICROSTRATEGY),
                        eq(response)
                );
    }

    @Test
    void execute_whenGetAdUserNameThrowsNpe_shouldRedirectViaAppStrategyUtil() throws Exception {
        // given: simulate a null username causing an NPE on .toLowerCase(),
        // which is also routed through the same catch block
        Users user = mock(Users.class);
        when(user.getAdUserName()).thenReturn(null);

        SHSApp app = mock(SHSApp.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String redirectUrl = "http://example.com/mstr";

        // when
        strategy.execute(user, redirectUrl, app, request, response);

        // then: loginAndLaunch is never reached because toLowerCase() NPEs first
        verify(mstrAuthService, never())
                .loginAndLaunch(anyString(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        verify(appStrategyUtil, times(1))
                .redirectToErrorPage(
                        eq("0"),
                        any(), // NPE message can vary by JDK version, so don't over-assert
                        eq(ApplicationConstants.MICROSTRATEGY),
                        eq(response)
                );
    }
}