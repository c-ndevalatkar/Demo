package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.VerificationTokenDTO;
import com.symphony.applaunch.entity.UserLogin;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import com.symphony.applaunch.service.ILoginService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component("JWTBuilder")
public class JWTBuilder {

	@Value("${jwt.secret}")
	public String secret;

	private ILoginService loginService;
	
	@Autowired
	LocaleMessageUtility messageUtility;
	
	@Autowired
	private EncryptionUtil encryptionUtil;

	private static final Logger logger = LoggerFactory.getLogger(JWTBuilder.class);

	public JWTBuilder(ILoginService loginService) {
		this.loginService = loginService;
	}

	public String getValue(String jwtToken, String claimType) {
		Claims claims = null;
		claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary("" + secret)).parseClaimsJws(jwtToken)
				.getBody();

		return (String) claims.get(claimType);
	}

	public VerificationTokenDTO validateToken(String jwtToken, boolean isTokenNotVerified) {
		try {
			VerificationTokenDTO verificationDTO = new VerificationTokenDTO();
			long nowMillis = System.currentTimeMillis();
			Date now = new Date(nowMillis);
			String email = null;
			Long loginId = null;
			Claims claims = null;
			if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
				jwtToken = jwtToken.substring(7);
			}
			claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary("" + secret))
					.parseClaimsJws(jwtToken).getBody();
			email = (String) claims.get(ApplicationConstants.EMAIL_ID);

			verificationDTO.setEmail(email);
			// check this condition
			if (claims.getExpiration().compareTo(now) < 0 || claims.getExpiration().compareTo(now) == 0) {
				if (isTokenNotVerified) {
					verificationDTO.setIsTokenExpired(true);
				} else {
					throw new ApplicationException("Token Expired", ErrorCode.EXPIRED_TOKEN.getCodeId(),
							HttpStatus.UNAUTHORIZED);
				}
			}

			Object loginId1 = claims.get(ApplicationConstants.LOGINID);
			logger.info("Login ID fetched from the claims: {}", loginId1);
			try {
				if (loginId1 == null) {
					throw new IllegalArgumentException("LoginID is missing in claims!");
				}
				loginId = Long.parseLong(loginId1.toString());
			} catch (Exception e) {
				logger.info("*****catch block from validateToken()- 1*****");
				logger.error(ApplicationConstants.CATCH_MESSAGE + e);
			}

			if (loginId != null) {
				UserLogin userLogin = loginService.getUserLogin(loginId);

				if (userLogin != null && !userLogin.getIsLogin()) {
					return null;
				}
			}

			return verificationDTO;
		} catch (Exception e) {
			logger.info("****catch block from validateToken()- 2*****");
			logger.info(ApplicationConstants.CATCH_MESSAGE + e);
			return null;
		}
	}

	public String createJWT(String email, String adUserName, String ssoAppId, Boolean isWebTicket) {

		logger.info("-----Inside createJWT method----");

		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// We will sign our JWT with our APIKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		Map<String, Object> claims = new HashMap<>();
		claims.put(ApplicationConstants.EMAIL_ID, email);
		claims.put(ApplicationConstants.AD_USER_NAME, adUserName);
		claims.put(ApplicationConstants.SSO_APP_ID, ssoAppId);

		logger.info("--------User Details from JWTBuilder---------");

		JwtBuilder builder = Jwts.builder().setIssuedAt(now).signWith(signatureAlgorithm, signingKey).setClaims(claims);

		// if it has been specified, let's add the expiration
		Calendar cal = Calendar.getInstance();
		cal.setTime(new java.sql.Timestamp(cal.getTime().getTime()));
		if (Boolean.TRUE.equals(isWebTicket))
			cal.add(Calendar.MINUTE, ApplicationConstants.EXPIRATION_THREE_MIN_TOKEN);
		else
			cal.add(Calendar.MINUTE, ApplicationConstants.EXPIRATION_TEN_HOURS);

		builder.setExpiration(new Date(cal.getTime().getTime()));
		// Builds the JWT and serializes it to a compact, URL-safe string

		String token = builder.compact();
		logger.info("----------TOKEN from JWTBuilder" + "Token- XXXX");
		return token;

	}

	/**
	 * This method is to create shortcut URl for applications
	 * @param appId
	 * @return
	 */
	public String createAppShortcutURL(Integer appId) {

		if (appId == null) {
			logger.info("appId is null");
			return null;
		}

		try {
			String appid = encryptionUtil.encryptData1(appId.toString());

			if (appid.contains("/"))
				appid = appid.replace("/", "SL");
			if (appid.contains("+"))
				appid = appid.replace("+", "PL");

			return appid;
		} catch (Exception e) {
			throw new ApplicationException(messageUtility.getMessage(ApplicationConstants.TOKEN_CREATION_ERROR),
					ErrorCode.INVALID_TOKEN.getCodeId(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
