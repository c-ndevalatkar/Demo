package com.symphony.applaunch.service;

import com.symphony.applaunch.entity.UserLogin;

public interface ILoginService {
    UserLogin getUserLogin(Long loginId);
}
