package com.symphony.applaunch.service;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.VerificationTokenDTO;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.repository.IUserDAO;
import com.symphony.applaunch.util.JWTBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private JWTBuilder jwtBuilder;

    @Mock
    private IUserDAO userDAO;

    @InjectMocks
    private UserService userService;

    // ---------- getUserFromToken(String token) ----------

    @Test
    public void getUserFromToken_okta_shouldReturnUserByEmail() {
        String token = "valid-okta-token";

        // validateToken returns non-null -> valid
        when(jwtBuilder.validateToken(eq(token), eq(false))).thenReturn(new VerificationTokenDTO());
        // authType = OKTA
        when(jwtBuilder.getValue(eq(token), eq(ApplicationConstants.COMPANY_AUTH_TYPE)))
                .thenReturn(ApplicationConstants.OKTA);
        // email from token
        String email = "user@example.com";
        when(jwtBuilder.getValue(eq(token), eq(ApplicationConstants.EMAIL_ID)))
                .thenReturn(email);

        Users dbUser = new Users();
        dbUser.setId(1L);
        when(userDAO.findByEmail(email)).thenReturn(dbUser);

        Users result = userService.getUserFromToken(token);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        verify(userDAO, times(1)).findByEmail(email);
        verify(userDAO, never()).findByAdName(anyString());
    }

    @Test
    public void getUserFromToken_nonOkta_shouldReturnUserByAdUsername() {
        String token = "valid-ad-token";

        when(jwtBuilder.validateToken(eq(token), eq(false))).thenReturn(new VerificationTokenDTO());
        // authType anything but OKTA
        when(jwtBuilder.getValue(eq(token), eq(ApplicationConstants.COMPANY_AUTH_TYPE)))
                .thenReturn("AD");
        String adUserName = "domain\\john.doe";
        when(jwtBuilder.getValue(eq(token), eq(ApplicationConstants.AD_USER_NAME)))
                .thenReturn(adUserName);

        Users dbUser = new Users();
        dbUser.setId(2L);
        when(userDAO.findByAdName(adUserName)).thenReturn(dbUser);

        Users result = userService.getUserFromToken(token);

        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.getId());
        verify(userDAO, times(1)).findByAdName(adUserName);
        verify(userDAO, never()).findByEmail(anyString());
    }

    @Test
    public void getUserFromToken_whenValidateTokenReturnsNull_shouldThrowApplicationException() {
        String token = "invalid-token";

        when(jwtBuilder.validateToken(eq(token), eq(false))).thenReturn(null);

        try {
            userService.getUserFromToken(token);
            fail("Expected ApplicationException to be thrown");
        } catch (ApplicationException ex) {
            assertEquals(ApplicationConstants.INVALID_TOKEN, ex.getMessage());
            assertEquals(ErrorCode.INVALID_TOKEN.getCodeId(), ex.getErrorCodeId());
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

    @Test
    public void getUserFromToken_whenJwtBuilderThrows_shouldWrapInApplicationException() {
        String token = "boom-token";

        when(jwtBuilder.validateToken(eq(token), eq(false)))
                .thenThrow(new RuntimeException("JWT service unavailable"));

        try {
            userService.getUserFromToken(token);
            fail("Expected ApplicationException to be thrown");
        } catch (ApplicationException ex) {
            assertEquals(ApplicationConstants.INVALID_TOKEN, ex.getMessage());
            assertEquals(ErrorCode.INVALID_TOKEN.getCodeId(), ex.getErrorCodeId());
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

    // ---------- getUserFromToken(HttpServletRequest request) ----------

    @Test
    public void getUserFromTokenRequest_valid_shouldReturnUser() {
        String token = "valid-token";
        String adUserName = "my-domain\\user1";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ApplicationConstants.AUTHORIZATION)).thenReturn(token);

        when(jwtBuilder.validateToken(eq(token), eq(false))).thenReturn(new VerificationTokenDTO());
        when(jwtBuilder.getValue(eq(token), eq(ApplicationConstants.AD_USER_NAME)))
                .thenReturn(adUserName);

        Users dbUser = new Users();
        dbUser.setId(10L);
        when(userDAO.findByAdName(adUserName)).thenReturn(dbUser);

        Users result = userService.getUserFromToken(request);

        assertNotNull(result);
        assertEquals(Long.valueOf(10L), result.getId());
        verify(userDAO, times(1)).findByAdName(adUserName);
    }

    @Test
    public void getUserFromTokenRequest_whenValidateTokenReturnsNull_shouldThrowApplicationException() {
        String token = "bad-token";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ApplicationConstants.AUTHORIZATION)).thenReturn(token);

        when(jwtBuilder.validateToken(eq(token), eq(false))).thenReturn(null);

        try {
            userService.getUserFromToken(request);
            fail("Expected ApplicationException to be thrown");
        } catch (ApplicationException ex) {
            assertEquals(ApplicationConstants.INVALID_TOKEN, ex.getMessage());
            assertEquals(ErrorCode.INVALID_TOKEN.getCodeId(), ex.getErrorCodeId());
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

    @Test
    public void getUserFromTokenRequest_whenAnyExceptionOccurs_shouldThrowApplicationException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // e.g. header null and validateToken throws NPE or some runtime
        when(request.getHeader(ApplicationConstants.AUTHORIZATION)).thenReturn(null);

        when(jwtBuilder.validateToken(isNull(), eq(false)))
                .thenThrow(new RuntimeException("boom"));

        try {
            userService.getUserFromToken(request);
            fail("Expected ApplicationException to be thrown");
        } catch (ApplicationException ex) {
            assertEquals(ApplicationConstants.INVALID_TOKEN, ex.getMessage());
            assertEquals(ErrorCode.INVALID_TOKEN.getCodeId(), ex.getErrorCodeId());
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

    // ---------- saveRandomGeneratedToken ----------

    @Test
    public void saveRandomGeneratedToken_shouldUpdateAndReturnUser() {
        Users userInput = new Users();
        userInput.setId(5L);
        userInput.setTokenType("RANDOM");

        Users userFromDb = new Users();
        userFromDb.setId(5L);

        when(userDAO.findOne(5L)).thenReturn(userFromDb);
        when(userDAO.updateUserData(any(Users.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String token = "generated-token-xyz";

        Users result = userService.saveRandomGeneratedToken(userInput, token);

        assertNotNull(result);
        assertEquals(Long.valueOf(5L), result.getId());
        assertEquals("RANDOM", result.getTokenType());
        assertEquals(token, result.getToken());
        assertNotNull("tokenGeneratedTimestamp should be set", result.getTokenGeneratedTimestamp());
        assertTrue("tokenGeneratedTimestamp should be 'recent'",
                result.getTokenGeneratedTimestamp().before(new Date(System.currentTimeMillis() + 1000)));

        verify(userDAO, times(1)).findOne(5L);
        verify(userDAO, times(1)).updateUserData(any(Users.class));
    }

    // ---------- getUserByEmailId / getUserByAdUsername ----------

    @Test
    public void getUserByEmailId_shouldDelegateToDao() {
        String email = "test@example.com";

        Users dbUser = new Users();
        dbUser.setId(11L);

        when(userDAO.findByEmail(email)).thenReturn(dbUser);

        Users result = userService.getUserByEmailId(email);

        assertNotNull(result);
        assertEquals(Long.valueOf(11L), result.getId());
        verify(userDAO, times(1)).findByEmail(email);
    }

    @Test
    public void getUserByAdUsername_shouldDelegateToDao() {
        String adName = "domain\\testuser";

        Users dbUser = new Users();
        dbUser.setId(12L);

        when(userDAO.findByAdName(adName)).thenReturn(dbUser);

        Users result = userService.getUserByAdUsername(adName);

        assertNotNull(result);
        assertEquals(Long.valueOf(12L), result.getId());
        verify(userDAO, times(1)).findByAdName(adName);
    }
}

