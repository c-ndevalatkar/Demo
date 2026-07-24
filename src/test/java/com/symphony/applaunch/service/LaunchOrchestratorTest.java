package com.symphony.applaunch.service;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.constants.LogEventTypeConstant;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.service.strategies.common.AppStrategyUtil;
import com.symphony.applaunch.util.ClientUtil;
import com.symphony.applaunch.util.EncryptionUtil;
import com.symphony.applaunch.util.LoggingUtility;
import com.symphony.applaunch.util.UserValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for LaunchOrchestrator using plain Mockito (no Spring context).
 */
class LaunchOrchestratorTest {

    private LaunchOrchestrator launchOrchestrator;

    private Map<String, LaunchStrategy> strategies;
    private UserService userService;
    private UserValidationUtil userValidationUtil;
    private IAppService appService;
    private ClientUtil clientUtil;
    private LoggingUtility loggingUtility;
    private AppStrategyUtil appStrategyUtil;

    @BeforeEach
    void setUp() throws Exception {
        strategies = new HashMap<>();
        userService = Mockito.mock(UserService.class);
        userValidationUtil = Mockito.mock(UserValidationUtil.class);
        appService = Mockito.mock(IAppService.class);
        clientUtil = Mockito.mock(ClientUtil.class);
        loggingUtility = Mockito.mock(LoggingUtility.class);
        appStrategyUtil = Mockito.mock(AppStrategyUtil.class);

        launchOrchestrator = new LaunchOrchestrator(
                strategies, userService, userValidationUtil, appService, clientUtil, loggingUtility, appStrategyUtil);

        // Set shsPortalUrl via reflection
        Field f = LaunchOrchestrator.class.getDeclaredField("shsPortalUrl");
        f.setAccessible(true);
        f.set(launchOrchestrator, "https://portal.example.com");
    }

    /**
     * Parameter source for all the "happy path with strategy" cases.
     *
     * appName-> request.getParameter(APPNAME)
     * appEntityName-> value returned from app.getName()
     * appId-> value used for app.getId() and logging refId
     */
    static Stream<Arguments> strategyCases() {
        return Stream.of(
                // corresponds to test_launch_happyPath_withStrategy_executes_MDTStrategy_AndLogs
                arguments("mdt", "mdt", "42"),
                // corresponds to test_launch_happyPath_withStrategy_executes_MSTRStrategy_AndLogs
                arguments("MICROSTRATEGY", "MICROSTRATEGY", "42"),
                // corresponds to test_launch_happyPath_withStrategy_executes_QlikSenseStrategy_AndLogs
                arguments("QlikSense", "QlikSense", "42"),
                // corresponds to test_launch_happyPath_withStrategy_executes_QlikViewStrategy_AndLogs
                arguments("QlikView", "My App", "42"),
                // corresponds to test_launch_happyPath_withStrategy_executes_MSTRCloudStrategy_AndLogs
                arguments("MicrosrtategyCloud", "MicrosrtategyCloud", "42"),
                // corresponds to test_launch_happyPath_withStrategy_executes_GenericStrategy_AndLogs
                arguments("Generic", "Generic", "43"),
                // corresponds to test_launch_happyPath_withStrategy_executesStrategyAndLogs (MYAPP)
                arguments("MYAPP", "My App", "42")
        );
    }

    @ParameterizedTest(name = "happy path: appName={0}, appEntityName={1}, appId={2}")
    @MethodSource("strategyCases")
    void test_launch_happyPath_withStrategy_executesStrategy_AndLogs(
            String appName,
            String appEntityName,
            String appId
    ) throws Exception {

        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String userIdStr = "123";
        Long userId = 123L;
        String encToken = "encryptedToken";
        String decryptedToken = "jwt-token";
        String redirectUrl = "https://app.example.com/start";

        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userIdStr);
        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn(encToken);
        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(redirectUrl);
        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);
        when(request.getParameter(ApplicationConstants.APPNAME)).thenReturn(appName);

        Users user = mock(Users.class);
        when(userService.getUserFromToken(decryptedToken)).thenReturn(user);
        when(userValidationUtil.isUserSameAsTokenUser(userId, user)).thenReturn(true);

        SHSApp app = mock(SHSApp.class);
        when(appService.getAppById(appId)).thenReturn(app);
        when(app.getId()).thenReturn(Integer.valueOf(appId));
        when(app.getUrl()).thenReturn(redirectUrl);
        when(app.getName()).thenReturn(appEntityName);

        // strategy present for this appName
        LaunchStrategy strategy = mock(LaunchStrategy.class);
        strategies.put(appName.toUpperCase(), strategy);

        when(clientUtil.getClientBrowser(request)).thenReturn("Chrome");
        when(loggingUtility.getIpAddr(request)).thenReturn("127.0.0.1");

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(encToken)).thenReturn(decryptedToken);

            // Act
            launchOrchestrator.launch(request, response);

            // Assert: strategy executed
            verify(strategy, times(1))
                    .execute(eq(user), eq(redirectUrl), eq(app), eq(request), eq(response));

            // logging was attempted
            verify(loggingUtility, times(1))
                    .logUserEvent(eq(user),
                            contains(LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER_MSG),
                            eq(LogEventTypeConstant.APPLICATION_LAUNCH_BY_USER),
                            eq("Chrome"),
                            eq(Long.parseLong(appId)),
                            isNull(),
                            eq("127.0.0.1"));

            // no error redirect
            verify(appStrategyUtil, never())
                    .redirectToErrorPage(anyString(), anyString(), anyString(), any(HttpServletResponse.class));
        }
    }

    @Test
    void test_launch_whenStrategyMissing_shouldRedirectToAppUrl() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String userIdStr = "123";
        Long userId = 123L;
        String encToken = "enc";
        String decryptedToken = "jwt-token";
        String redirectUrl = "https://app.example.com/start";
        String appId = "42";
        String appName = "UNKNOWNAPP";

        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userIdStr);
        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn(encToken);
        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(redirectUrl);
        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);
        when(request.getParameter(ApplicationConstants.APPNAME)).thenReturn(appName);

        Users user = mock(Users.class);
        when(userService.getUserFromToken(decryptedToken)).thenReturn(user);
        when(userValidationUtil.isUserSameAsTokenUser(userId, user)).thenReturn(true);

        SHSApp app = mock(SHSApp.class);
        when(appService.getAppById(appId)).thenReturn(app);
        when(app.getUrl()).thenReturn(redirectUrl);
        when(app.getName()).thenReturn("Some Name");
        when(app.getId()).thenReturn(Integer.valueOf(appId));

        // strategies map intentionally does NOT contain this key

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(encToken)).thenReturn(decryptedToken);

            // Act
            launchOrchestrator.launch(request, response);

            // Assert: since strategy is null, redirect to redirectUrl
            verify(response, times(1)).sendRedirect(redirectUrl);
        }
    }

    @Test
    void test_launch_whenUserServiceThrowsApplicationException_shouldRedirectToLogin() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String userIdStr = "123";
        String encToken = "enc";
        String decryptedToken = "jwt-token";
        String redirectUrl = "https://app.example.com/start";

        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userIdStr);
        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn(encToken);
        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(redirectUrl);

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(encToken)).thenReturn(decryptedToken);

            when(userService.getUserFromToken(decryptedToken))
                    .thenThrow(new ApplicationException(ApplicationConstants.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getCodeId(),
                            HttpStatus.UNAUTHORIZED));

            // act & assert
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> launchOrchestrator.launch(request, response));
            assertEquals(ApplicationConstants.UNAUTHORIZED_ACCESS, ex.getReason());
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

            // Assert: should redirect to portal login with error=23
            verify(response, times(1))
                    .sendRedirect("https://portal.example.com/#/login?login_error=23");

            // And should not proceed to userValidation or appService
            Long userId = 123L; // same as in the test arrange

            // userValidationUtil is invoked with null loggedInuserEntity
            verify(userValidationUtil, times(1))
                    .isUserSameAsTokenUser(eq(userId), isNull());
            verify(appService, never()).getAppById(anyString());
        }
    }

    @Test
    void test_launch_whenUserValidationFails_shouldThrowForbidden() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String userIdStr = "123";
        Long userId = 123L;
        String encToken = "enc";
        String decryptedToken = "jwt";
        String redirectUrl = "https://app.example.com/start";
        String appId = "42";

        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userIdStr);
        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn(encToken);
        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(redirectUrl);
        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);

        Users user = mock(Users.class);

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(encToken)).thenReturn(decryptedToken);

            when(userService.getUserFromToken(decryptedToken)).thenReturn(user);
            when(userValidationUtil.isUserSameAsTokenUser(userId, user)).thenReturn(false);

            // Act + Assert
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> launchOrchestrator.launch(request, response));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals(ApplicationConstants.UNAUTHORIZED_ACCESS, ex.getReason());
        }
    }

    @Test
    void test_launch_whenRedirectUrlDoesNotMatchAppUrl_shouldThrowForbiddenBadRequest() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String userIdStr = "123";
        Long userId = 123L;
        String encToken = "enc";
        String decryptedToken = "jwt";
        String redirectUrl = "https://app.example.com/start";
        String appId = "42";

        when(request.getParameter(ApplicationConstants.USER_ID)).thenReturn(userIdStr);
        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn(encToken);
        when(request.getParameter(ApplicationConstants.REDIRECT_URL)).thenReturn(redirectUrl);
        when(request.getParameter(ApplicationConstants.APP_ID)).thenReturn(appId);

        Users user = mock(Users.class);
        SHSApp app = mock(SHSApp.class);

        when(appService.getAppById(appId)).thenReturn(app);
        when(app.getUrl()).thenReturn("https://some-other-url.example.com");
        when(app.getId()).thenReturn(Integer.valueOf(appId));

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(encToken)).thenReturn(decryptedToken);

            when(userService.getUserFromToken(decryptedToken)).thenReturn(user);
            when(userValidationUtil.isUserSameAsTokenUser(eq(userId), eq(user))).thenReturn(true);

            // Act + Assert
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> launchOrchestrator.launch(request, response));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals(ApplicationConstants.BAD_REQUEST, ex.getReason());
        }
    }
}
