package com.symphony.applaunch.controller;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.VerificationTokenDTO;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.service.LaunchOrchestrator;
import com.symphony.applaunch.service.UserService;
import com.symphony.applaunch.service.impl.AppServiceImpl;
import com.symphony.applaunch.util.ConversionUtil;
import com.symphony.applaunch.util.EncryptionUtil;
import com.symphony.applaunch.util.JWTBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockingDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppLaunchControllerTest {

    private MockMvc mockMvc;
    private LaunchOrchestrator orchestrator;
    private JWTBuilder jwtBuilder;
    private AppLaunchController controller;
	private  ConversionUtil convertDtoToEntity;
	private  UserService userService;
	private  AppServiceImpl appService;

    @BeforeEach
    void setUp() {
        // create mocks directly with Mockito
        orchestrator = Mockito.mock(LaunchOrchestrator.class);
        jwtBuilder = Mockito.mock(JWTBuilder.class);

        // quick sanity that jwtBuilder is a mock
        MockingDetails details = Mockito.mockingDetails(jwtBuilder);
        assertTrue(details.isMock(), "jwtBuilder must be a Mockito mock");

        // create controller with the mocks
        controller = new AppLaunchController(orchestrator, jwtBuilder, userService, convertDtoToEntity, appService);

        // build standalone MockMvc for this controller (no Spring context)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void test_launch_withBearerHeader_and_validToken_shouldCallOrchestrator_andReturn200() throws Exception {
        String bearer = "Bearer someSuperLongTokenValue1234567890";

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(anyString())).thenReturn("someSuperLongTokenValue1234567890");

            when(jwtBuilder.validateToken(eq("someSuperLongTokenValue1234567890"), eq(false)))
                    .thenReturn(new VerificationTokenDTO());

            mockMvc.perform(post("/api/launch")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(ApplicationConstants.AUTHORIZATION, bearer)
                            .param(ApplicationConstants.AUTHORIZATION, "encrypted-placeholder"))
                    .andExpect(status().isOk());

            verify(orchestrator, times(1)).launch(any(HttpServletRequest.class), any(HttpServletResponse.class));
        }
    }

    @Test
    void test_launch_withRawHeader_and_validToken_shouldCallOrchestrator() throws Exception {
        String raw = "rawToken12345";

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(anyString())).thenReturn("someDecryptedValue");

            when(jwtBuilder.validateToken(eq(raw), eq(false))).thenReturn(new VerificationTokenDTO());

            mockMvc.perform(post("/api/launch")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(ApplicationConstants.AUTHORIZATION, raw)
                            .param(ApplicationConstants.AUTHORIZATION, "enc"))
                    .andExpect(status().isOk());

            verify(orchestrator, times(1)).launch(any(HttpServletRequest.class), any(HttpServletResponse.class));
        }
    }

    @Test
    void test_launch_withShortHeader_and_validToken_shouldCallOrchestrator() throws Exception {
        String shortHeader = "shortHdr";

        try (MockedStatic<EncryptionUtil> encMock = Mockito.mockStatic(EncryptionUtil.class)) {
            encMock.when(() -> EncryptionUtil.decryptData(anyString())).thenReturn("decrypted-placeholder");

            when(jwtBuilder.validateToken(eq(shortHeader), eq(false))).thenReturn(new VerificationTokenDTO());

            mockMvc.perform(post("/api/launch")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(ApplicationConstants.AUTHORIZATION, shortHeader)
                            .param(ApplicationConstants.AUTHORIZATION, "enc"))
                    .andExpect(status().isOk());

            verify(orchestrator, times(1)).launch(any(HttpServletRequest.class), any(HttpServletResponse.class));
        }
    }

    @Test
    void test_unit_launch_noHeader_invalidToken_throwsApplicationException() throws Exception {
        // arrange: mocks
        LaunchOrchestrator orchestrator = Mockito.mock(LaunchOrchestrator.class);
        JWTBuilder jwtBuilder = Mockito.mock(JWTBuilder.class);
        controller = new AppLaunchController(orchestrator, jwtBuilder, userService, convertDtoToEntity, appService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter(ApplicationConstants.AUTHORIZATION)).thenReturn("enc-val");
        when(request.getHeader(ApplicationConstants.AUTHORIZATION)).thenReturn(null);

        try (MockedStatic<EncryptionUtil> enc = Mockito.mockStatic(EncryptionUtil.class)) {
            enc.when(() -> EncryptionUtil.decryptData("enc-val")).thenReturn("decryptedToken");

            // arrange: jwtBuilder returns null to indicate invalid token
            when(jwtBuilder.validateToken(isNull(), eq(false))).thenReturn(null);

            // act & assert
            ApplicationException ex = assertThrows(ApplicationException.class, () -> controller.launch(request, response));

            assertEquals(ApplicationConstants.INVALID_TOKEN, ex.getMessage());
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }
}
