package com.symphony.applaunch.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.symphony.applaunch.service.IHealthCheckService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
class HealthCheckControllerTest {

    @Mock
    private IHealthCheckService healthService;

    @InjectMocks
    private HealthCheckController healthCheckController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(healthCheckController)
            .build();
    }

    @Test
    void testGetHealthStatus_WhenHealthy_ReturnsHealthyMessage() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("APP LAUNCH-SERVICE - HEALTHY"));

        verify(healthService, times(1)).checkApplicationHealth();
    }

    @Test
    void testGetHealthStatus_WhenUnhealthy_ReturnsUnhealthyMessage() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("APP LAUNCH-SERVICE - UNHEALTHY"));

        verify(healthService, times(1)).checkApplicationHealth();
    }

    @Test
    void testGetHealthStatus_VerifiesServiceInteraction() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act
        mockMvc.perform(get("/healthcheck/status"));

        // Assert
        verify(healthService, times(1)).checkApplicationHealth();
        verifyNoMoreInteractions(healthService);
    }

    @Test
    void testGetHealthStatus_WhenHealthy_ContainsHealthyKeyword() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("HEALTHY")));
    }

    @Test
    void testGetHealthStatus_WhenUnhealthy_ContainsUnhealthyKeyword() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("UNHEALTHY")));
    }

    @Test
    void testGetHealthStatus_MultipleCallsReturnConsistentResults() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act & Assert - First call
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("APP LAUNCH-SERVICE - HEALTHY"));

        // Act & Assert - Second call
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("APP LAUNCH-SERVICE - HEALTHY"));

        verify(healthService, times(2)).checkApplicationHealth();
    }

    @Test
    void testGetHealthStatus_EndpointMapping() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetHealthStatus_ReturnsPlainText() throws Exception {
        // Arrange
        when(healthService.checkApplicationHealth()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/healthcheck/status"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

}
