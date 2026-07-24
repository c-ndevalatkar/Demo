package com.symphony.applaunch.service.impl;

import com.symphony.applaunch.entity.UserLogin;
import com.symphony.applaunch.repository.IUserLoginDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceImplTest {

    private IUserLoginDAO userLoginDAO;
    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        userLoginDAO = Mockito.mock(IUserLoginDAO.class);
        loginService = new LoginServiceImpl(userLoginDAO);
    }

    @Test
    void getUserLogin_whenUserExists_shouldReturnUserLogin() {
        // given
        Long loginId = 1L;
        UserLogin userLogin = new UserLogin();
        userLogin.setUserLoginId(loginId);

        when(userLoginDAO.get(loginId)).thenReturn(userLogin);

        // when
        UserLogin result = loginService.getUserLogin(loginId);

        // then
        assertNotNull(result);
        assertSame(userLogin, result);  // same instance returned
        verify(userLoginDAO, times(1)).get(loginId);
        verifyNoMoreInteractions(userLoginDAO);
    }

    @Test
    void getUserLogin_whenUserDoesNotExist_shouldReturnNull() {
        // given
        Long loginId = 99L;
        when(userLoginDAO.get(loginId)).thenReturn(null);

        // when
        UserLogin result = loginService.getUserLogin(loginId);

        // then
        assertNull(result);
        verify(userLoginDAO, times(1)).get(loginId);
        verifyNoMoreInteractions(userLoginDAO);
    }
}
