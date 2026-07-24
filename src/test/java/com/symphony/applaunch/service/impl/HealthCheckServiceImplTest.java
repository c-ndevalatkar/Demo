package com.symphony.applaunch.service.impl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.applaunch.repository.IHealthCheckDao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceImplTest {

	@Mock
	private IHealthCheckDao healthcheckDao;

	@InjectMocks
	private HealthCheckServiceImpl healthCheckService;

	@BeforeEach
	void setUp() {

	}

	@Test
	void testCheckApplicationHealth_WhenDatabaseIsHealthy_ReturnsTrue() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(true);

		// Act
		boolean result = healthCheckService.checkApplicationHealth();

		// Assert
		assertTrue(result, "Application health should be true when database is healthy");
		verify(healthcheckDao, times(1)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_WhenDatabaseIsUnhealthy_ReturnsFalse() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(false);

		// Act
		boolean result = healthCheckService.checkApplicationHealth();

		// Assert
		assertFalse(result, "Application health should be false when database is unhealthy");
		verify(healthcheckDao, times(1)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_VerifiesDaoIsCalledOnce() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(true);

		// Act
		healthCheckService.checkApplicationHealth();

		// Assert
		verify(healthcheckDao, times(1)).checkDbConnection();
		verifyNoMoreInteractions(healthcheckDao);
	}

	@Test
	void testCheckApplicationHealth_MultipleCalls_EachCallsDao() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(true);

		// Act
		healthCheckService.checkApplicationHealth();
		healthCheckService.checkApplicationHealth();
		healthCheckService.checkApplicationHealth();

		// Assert
		verify(healthcheckDao, times(3)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_WhenDaoThrowsRuntimeException_PropagatesException() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenThrow(new RuntimeException("Database connection failed"));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> {
			healthCheckService.checkApplicationHealth();
		}, "Should propagate RuntimeException from DAO");

		verify(healthcheckDao, times(1)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_WhenDaoThrowsNullPointerException_PropagatesException() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenThrow(new NullPointerException("DAO is null"));

		// Act & Assert
		assertThrows(NullPointerException.class, () -> {
			healthCheckService.checkApplicationHealth();
		}, "Should propagate NullPointerException from DAO");

		verify(healthcheckDao, times(1)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_WhenDaoThrowsException_VerifiesExceptionMessage() {
		// Arrange
		String expectedMessage = "Connection timeout";
		when(healthcheckDao.checkDbConnection()).thenThrow(new RuntimeException(expectedMessage));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			healthCheckService.checkApplicationHealth();
		});

		assertEquals(expectedMessage, exception.getMessage());
		verify(healthcheckDao, times(1)).checkDbConnection();
	}
	// ========== EDGE CASE TESTS ==========

	@Test
	void testCheckApplicationHealth_ConsecutiveCallsWithDifferentResults() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(true).thenReturn(false).thenReturn(true);

		// Act & Assert
		assertTrue(healthCheckService.checkApplicationHealth(), "First call should return true");
		assertFalse(healthCheckService.checkApplicationHealth(), "Second call should return false");
		assertTrue(healthCheckService.checkApplicationHealth(), "Third call should return true");

		verify(healthcheckDao, times(3)).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_ReturnsExactValueFromDao() {
		// Arrange - Test with true
		when(healthcheckDao.checkDbConnection()).thenReturn(true);

		// Act
		boolean resultTrue = healthCheckService.checkApplicationHealth();

		// Assert
		assertEquals(true, resultTrue, "Should return exact value from DAO (true)");

		// Arrange - Test with false
		when(healthcheckDao.checkDbConnection()).thenReturn(false);

		// Act
		boolean resultFalse = healthCheckService.checkApplicationHealth();

		// Assert
		assertEquals(false, resultFalse, "Should return exact value from DAO (false)");
	}

	@Test
	void testCheckApplicationHealth_DoesNotModifyDaoResult() {
		// Arrange
		when(healthcheckDao.checkDbConnection()).thenReturn(true);

		// Act
		boolean result = healthCheckService.checkApplicationHealth();

		// Assert - Verify the result is not modified
		assertTrue(result);
		verify(healthcheckDao).checkDbConnection();
	}

	@Test
	void testCheckApplicationHealth_NoDaoInteractionBeforeMethodCall() {
		// Assert - No interaction before calling the method
		verifyNoInteractions(healthcheckDao);

		// Act
		when(healthcheckDao.checkDbConnection()).thenReturn(true);
		healthCheckService.checkApplicationHealth();

		// Assert - Interaction happened after method call
		verify(healthcheckDao, times(1)).checkDbConnection();
	}
}
