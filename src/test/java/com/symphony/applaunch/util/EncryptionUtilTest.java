package com.symphony.applaunch.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.applaunch.entity.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 16-byte AES key: "0123456789abcdef"
    // Base64 = "MDEyMzQ1Njc4OWFiY2RlZg=="
    private static final String VALID_BASE64_KEY = "MDEyMzQ1Njc4OWFiY2RlZg==";

    // invalid key: "short" (5 bytes)
    private static final String INVALID_BASE64_KEY = Base64.getEncoder().encodeToString("short".getBytes());

    @BeforeEach
    void setUp() throws Exception {
        encryptionUtil = new EncryptionUtil();
        // by default, use a valid AES key for tests that expect success
        setSecretOnUtil(VALID_BASE64_KEY);
    }

    private void setSecretOnUtil(String base64Secret) throws Exception {
        Field secretField = EncryptionUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(encryptionUtil, base64Secret);
    }

    // --- static methods ---

    @Test
    void encryptData_shouldReturnSameString() {
        String input = "plain-text";
        String result = EncryptionUtil.encryptData(input);
        assertEquals(input, result);
    }

    @Test
    void decryptData_shouldReturnSameString() {
        String input = "encrypted-text";
        String result = EncryptionUtil.decryptData(input);
        assertEquals(input, result);
    }

    @Test
    void encryptData_withNull_shouldReturnNull() {
        String result = EncryptionUtil.encryptData(null);
        assertNull(result);
    }

    @Test
    void decryptData_withNull_shouldReturnNull() {
        String result = EncryptionUtil.decryptData(null);
        assertNull(result);
    }

    // --- encryptClaimType ---

    @Test
    void encryptClaimType_withNullClaimType_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> encryptionUtil.encryptClaimType(null));
        assertEquals("claimType cannot be null", ex.getMessage());
    }

    @Test
    void encryptClaimType_withInvalidSecretLength_shouldThrowIllegalArgumentException() throws Exception {
        // override default valid key with invalid one
        setSecretOnUtil(INVALID_BASE64_KEY);

        UserRoles claimType = new UserRoles(); // assume no-arg constructor exists

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> encryptionUtil.encryptClaimType(claimType));
        assertTrue(ex.getMessage().startsWith("AES key must be 16/24/32 bytes."),
                "Expected message to mention invalid AES key length");
    }

    @Test
    void encryptClaimType_withValidSecret_shouldReturnNonEmptyBase64String() throws Exception {
        UserRoles claimType = new UserRoles();

        String encrypted = encryptionUtil.encryptClaimType(claimType);

        assertNotNull(encrypted);
        assertFalse(encrypted.isBlank());

        // Ensure it is valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encrypted));
    }

    // --- decryptClaimType ---

    @Test
    void decryptClaimType_withNullEncryptedClaimType_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> encryptionUtil.decryptClaimType(null));
        assertEquals("encryptedClaimType cannot be null", ex.getMessage());
    }

    @Test
    void decryptClaimType_withInvalidSecretLength_shouldThrowIllegalArgumentException() throws Exception {
        setSecretOnUtil(INVALID_BASE64_KEY);

        // use some dummy Base64 — the method should fail before actual decrypt
        String dummyEncrypted = Base64.getEncoder().encodeToString("dummy".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> encryptionUtil.decryptClaimType(dummyEncrypted));
        assertTrue(ex.getMessage().startsWith("AES key must be 16/24/32 bytes."),
                "Expected message to mention invalid AES key length");
    }

    @Test
    void encryptAndDecryptClaimType_withValidSecret_shouldRoundTrip() throws Exception {
        // Arrange a UserRoles instance
        UserRoles original = new UserRoles();
        // If UserRoles has fields, you can set some of them here to make the test stronger

        // Act
        String encrypted = encryptionUtil.encryptClaimType(original);
        UserRoles decrypted = encryptionUtil.decryptClaimType(encrypted);

        // Assert: compare JSON to avoid relying on equals() implementation
        String originalJson = objectMapper.writeValueAsString(original);
        String decryptedJson = objectMapper.writeValueAsString(decrypted);

        assertEquals(originalJson, decryptedJson, "Decrypted UserRoles should match original JSON representation");
    }
}

