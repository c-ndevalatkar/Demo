package com.symphony.applaunch.repository.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;  // ⚠️ Not actually used in current implementation

    @InjectMocks
    private HealthCheckDaoImpl healthCheckDao;

    @Test
    void testCheckDbConnection_AlwaysReturnsTrue() {
        // Act
        boolean result = healthCheckDao.checkDbConnection();

        // Assert
        assertTrue(result, "Should always return true since query is commented out");
        
        // verify(jdbcTemplate, never()).queryForObject(anyString(), any());
    }

    @Test
    void testCheckDbConnection_DoesNotCallJdbcTemplate() {
        // Act
        healthCheckDao.checkDbConnection();

        // Assert - Verify jdbcTemplate is NEVER called
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void testCheckDbConnection_MultipleCalls_AlwaysReturnsTrue() {
        // Act
        boolean result1 = healthCheckDao.checkDbConnection();
        boolean result2 = healthCheckDao.checkDbConnection();
        boolean result3 = healthCheckDao.checkDbConnection();

        // Assert
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
        
        // Verify jdbcTemplate is NEVER called
        verifyNoInteractions(jdbcTemplate);
    }
}
