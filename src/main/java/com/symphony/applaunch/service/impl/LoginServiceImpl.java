package com.symphony.applaunch.service.impl;

import com.symphony.applaunch.entity.UserLogin;
import com.symphony.applaunch.repository.IUserLoginDAO;
import com.symphony.applaunch.service.ILoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service("loginService")
@Transactional
public class LoginServiceImpl implements ILoginService {

    private final IUserLoginDAO userLoginDAO;

    @Override
    public UserLogin getUserLogin(Long loginId) {
        return userLoginDAO.get(loginId);
    }
}
