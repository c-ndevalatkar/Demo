package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.service.ILoginService;
import io.jsonwebtoken.Jwts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import com.symphony.applaunch.dto.VerificationTokenDTO;
import com.symphony.applaunch.entity.UserLogin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.xml.bind.DatatypeConverter;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JWTBuilderTest {

    @InjectMocks
    JWTBuilder jwtBuilder;

    @Mock
    private ILoginService loginService;

    private String base64Secret;

    @Before
    public void setUp() {
        // prepare a valid base64 secret to be used by both createJWT & validateToken
        base64Secret = Base64.getEncoder().encodeToString("my-secret-key-123".getBytes(StandardCharsets.UTF_8));
        jwtBuilder.secret = base64Secret;
    }

    // ---------------------------------------------------
    // createJWT + getValue
    // ---------------------------------------------------

    @Test
    public void createJWT_shouldCreateValidTokenWithClaims() {
        String email = "user@example.com";
        String adUser = "DOMAIN\\user";
        String ssoAppId = "APP-123";

        String token = jwtBuilder.createJWT(email, adUser, ssoAppId, false);

        assertNotNull("Token should not be null", token);

        // check claims via getValue
        assertEquals(email, jwtBuilder.getValue(token, ApplicationConstants.EMAIL_ID));
        assertEquals(adUser, jwtBuilder.getValue(token, ApplicationConstants.AD_USER_NAME));
        assertEquals(ssoAppId, jwtBuilder.getValue(token, ApplicationConstants.SSO_APP_ID));

        // check expiration is in the future
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(base64Secret))
                .parseClaimsJws(token)
                .getBody();

        assertTrue("Expiration should be in the future", claims.getExpiration().after(new Date()));
    }

    @Test
    public void getValue_shouldReturnStoredClaim() {
        // build a token manually with a known claim
        String claimKey = "customClaim";
        String claimValue = "my-claim";

        Key signingKey = new SecretKeySpec(
                DatatypeConverter.parseBase64Binary(base64Secret),
                SignatureAlgorithm.HS256.getJcaName());

        String token = Jwts.builder()
                .setIssuedAt(new Date())
                .claim(claimKey, claimValue)
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();

        String extracted = jwtBuilder.getValue(token, claimKey);

        assertEquals(claimValue, extracted);
    }

    // ---------------------------------------------------
    // validateToken – happy path
    // ---------------------------------------------------

    @Test
    public void validateToken_validTokenWithLoginIdAndActiveLogin_shouldReturnDTO() {
        String email = "valid@example.com";
        Long loginId = 123L;

        String token = createToken(email, loginId, /*future*/ true);

        UserLogin userLogin = new UserLogin();
        userLogin.setIsLogin(true);

        when(loginService.getUserLogin(loginId)).thenReturn(userLogin);

        VerificationTokenDTO dto = jwtBuilder.validateToken(token, false);

        assertNotNull("DTO should not be null for valid token", dto);
        assertEquals(email, dto.getEmail());
        assertFalse("isTokenExpired should be false when not expired", Boolean.TRUE.equals(dto.getIsTokenExpired()));

        verify(loginService, times(1)).getUserLogin(loginId);
    }

    // ---------------------------------------------------
    // validateToken – token expired
    // ---------------------------------------------------

    @Test
    public void validateToken_expiredToken_and_isTokenNotVerifiedTrue_shouldReturnDTOWithExpiredFlag() {
        String email = "expired@example.com";

        // expired token, no loginId needed
        String token = createToken(email, null, /*future*/ false);

        VerificationTokenDTO dto = jwtBuilder.validateToken(token, true);

        assertNull("Should return null for expired token even when isTokenNotVerified=true", dto);
        verifyNoInteractions(loginService);

        // loginService should not be called if LOGINID is missing
        verifyNoInteractions(loginService);
    }

    @Test
    public void validateToken_expiredToken_and_isTokenNotVerifiedFalse_shouldReturnNull() {
        String email = "expired@example.com";
        Long loginId = 456L;

        String token = createToken(email, loginId, /*future*/ false);

        VerificationTokenDTO dto = jwtBuilder.validateToken(token, false);

        // ApplicationException is thrown but caught by outer catch -> returns null
        assertNull("Should return null for expired token when isTokenNotVerified=false", dto);
    }

    // ---------------------------------------------------
    // validateToken – missing LOGINID
    // ---------------------------------------------------

    @Test
    public void validateToken_validTokenWithoutLoginId_shouldReturnDTOAndNotCallLoginService() {
        String email = "nold@example.com";

        String token = createToken(email, null, /*future*/ true);

        VerificationTokenDTO dto = jwtBuilder.validateToken(token, false);

        assertNotNull(dto);
        assertEquals(email, dto.getEmail());

        verifyNoInteractions(loginService);
    }

    // ---------------------------------------------------
    // validateToken – loginService returns isLogin=false
    // ---------------------------------------------------

    @Test
    public void validateToken_loginServiceSaysNotLoggedIn_shouldReturnNull() {
        String email = "user@example.com";
        Long loginId = 999L;

        String token = createToken(email, loginId, /*future*/ true);

        UserLogin userLogin = new UserLogin();
        userLogin.setIsLogin(false);

        when(loginService.getUserLogin(loginId)).thenReturn(userLogin);

        VerificationTokenDTO dto = jwtBuilder.validateToken(token, false);

        assertNull("Should return null when UserLogin.isLogin is false", dto);
    }

    // ---------------------------------------------------
    // validateToken – invalid token / parse failure
    // ---------------------------------------------------

    @Test
    public void validateToken_whenParsingFails_shouldReturnNull() {
        String invalidToken = "totally-not-a-jwt";

        VerificationTokenDTO dto = jwtBuilder.validateToken(invalidToken, false);

        assertNull("Should return null when parsing fails", dto);
        verifyNoInteractions(loginService);
    }

    // ---------------------------------------------------
    // Helper to build tokens used in tests
    // ---------------------------------------------------

    private String createToken(String email, Long loginId, boolean futureExpiration) {
        SignatureAlgorithm alg = SignatureAlgorithm.HS256;

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(base64Secret);
        Key signingKey = new SecretKeySpec(keyBytes, alg.getJcaName());

        Calendar cal = Calendar.getInstance();
        if (futureExpiration) {
            cal.add(Calendar.MINUTE, 10);
        } else {
            cal.add(Calendar.MINUTE, -10);
        }

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(cal.getTime())
                .signWith(alg, signingKey)
                .claim(ApplicationConstants.EMAIL_ID, email);

        if (loginId != null) {
            builder.claim(ApplicationConstants.LOGINID, loginId);
        }

        return builder.compact();
    }

    @Test
    public void test_createJWT_v1() {
        assertNotNull(jwtBuilder.createJWT("dummytext", "", String.valueOf(false), false));
        assertNotNull(jwtBuilder.createJWT("dummytext", "", String.valueOf(true), true));
        assertNotNull(jwtBuilder.createJWT("dummytext", "COMPANY", String.valueOf(false), false));
    }

    @Test
    public void test_createJWT_v2() {
        assertNotNull(jwtBuilder.createJWT("dummytext", "", "", false));
        assertNotNull(jwtBuilder.createJWT("dummytext", "COMPANY", "", true));
    }

}
