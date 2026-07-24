package com.symphony.applaunch.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.symphony.applaunch.entity.UserRoles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Component("encryptionUtil")
@Slf4j
public class EncryptionUtil {

	@Value("${jwt.secret}")
	public String secret;

	/**
	 * This method is used to encryt the user password
	 */
	public static String encryptData(String stringToEcrypt) {
		return stringToEcrypt;
	}

	/**
	 * This method is used to decrypt the encrypted password for the user
	 */
	public static String decryptData(String stringToEcrypt) {
		return stringToEcrypt;
	}

	/**
	 * This method is used to encrypt claimType (userRole) while creating token for
	 * user after login
	 */
	public String encryptClaimType(UserRoles claimType) throws Exception {
		if (claimType == null) {
			throw new IllegalArgumentException("claimType cannot be null");
		}
		byte[] raw = Base64.getDecoder().decode(secret);
		if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
			throw new IllegalArgumentException("AES key must be 16/24/32 bytes. Got: " + raw.length);
		}
		SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");
		String serializedClaimType = serializeClaimTypeToString(claimType);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding" /* NOSONAR */);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] claimTypeBytes = serializedClaimType.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedBytes = cipher.doFinal(claimTypeBytes);
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	private String serializeClaimTypeToString(UserRoles claimType) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(claimType);
	}

	/**
	 * This method is used to decrpt claimType (userRole) from the token
	 */
	public UserRoles decryptClaimType(String encryptedClaimType) throws Exception {
		if (encryptedClaimType == null) {
			throw new IllegalArgumentException("encryptedClaimType cannot be null");
		}
		byte[] raw = Base64.getDecoder().decode(secret);
		if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
			throw new IllegalArgumentException("AES key must be 16/24/32 bytes. Got: " + raw.length);
		}
		SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");

		byte[] encryptedBytes = Base64.getDecoder().decode(encryptedClaimType);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding" /* NOSONAR */);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		String decryptedClaimTypeString = new String(decryptedBytes, StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(decryptedClaimTypeString, UserRoles.class);
	}

	/**
	 * This method is used to encryt the user password
	 * 
	 * @param stringToEcrypt
	 * @return
	 */
	public String encryptData1(String stringToEcrypt) {
		CryptoLibrary cryptoLibrary = new CryptoLibrary();
		return cryptoLibrary.encrypt(stringToEcrypt);
	}

	/**
	 * This method is used to decrypt the encrypted password for the user
	 * 
	 * @param stringToEcrypt
	 * @return
	 */
	public String decryptData1(String stringToEcrypt) {
		CryptoLibrary cryptoLibrary = new CryptoLibrary();
		return cryptoLibrary.decrypt(decodeTokenFromURL(stringToEcrypt));
	}

	private static String decodeTokenFromURL(String token) {
		token = token.replaceAll("%20", "+");
		token = token.replaceAll(" ", "+");
		token = token.replaceAll("%2B", "+");
		token = token.replaceAll("%2F", "/");
		token = token.replaceAll("%3D", "=");
		return token;
	}

}
