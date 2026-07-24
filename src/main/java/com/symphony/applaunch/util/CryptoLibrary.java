package com.symphony.applaunch.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;

public class CryptoLibrary {

	private Cipher encryptCipher;

	private Cipher decryptCipher;

	private Encoder encoder = Base64.getEncoder();

	private Decoder decoder = Base64.getDecoder();

	public CryptoLibrary() throws SecurityException {
		java.security.Security.getProvider("SunJCE");

		char[] pass = "CHANGE THIS TO A BUNCH OF RANDOM CHARACTERS".toCharArray();

		byte[] salt = {

				(byte) 0xa3, (byte) 0x21, (byte) 0x24, (byte) 0x2c,

				(byte) 0xf2, (byte) 0xd2, (byte) 0x3e, (byte) 0x19 };

		init(pass, salt);
	}

	public void init(char[] pass, byte[] salt) {
		try {
			PBEParameterSpec ps = new javax.crypto.spec.PBEParameterSpec(salt, 20);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");

			SecretKey k = kf.generateSecret(new javax.crypto.spec.PBEKeySpec(pass));

			encryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding"); //NOSONAR
			encryptCipher.init(Cipher.ENCRYPT_MODE, k, ps);
			decryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding"); //NOSONAR
			decryptCipher.init(Cipher.DECRYPT_MODE, k, ps);

		} catch (Exception e) {
			throw new SecurityException("Could not initialize CryptoLibrary: " + e.getMessage());
		}
	}

	/**
	 * convenience method for encrypting a string.
	 * 
	 * @param str Description of the Parameter
	 * @return String the encrypted string.
	 * @exception SecurityException Description of the Exception
	 */
	public synchronized String encrypt(String str) {
		try {
			byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
			byte[] enc = encryptCipher.doFinal(utf8);
			return new String(encoder.encode(enc));
		} catch (Exception e) {
			throw new SecurityException("Could not encrypt: " + e.getMessage());
		}
	}

	/**
	 * convenience method for encrypting a string.
	 * 
	 * @param str Description of the Parameter
	 * @return String the encrypted string.
	 * @exception SecurityException Description of the Exception
	 */
	public synchronized String decrypt(String str) {
		try {
			byte[] dec = decoder.decode(str);
			byte[] utf8 = decryptCipher.doFinal(dec);
			return new String(utf8, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new SecurityException("Could not decrypt: " + e.getMessage());
		}
	}
}
