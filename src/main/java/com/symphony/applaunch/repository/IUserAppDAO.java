package com.symphony.applaunch.repository;

import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import com.symphony.applaunch.entity.Users;

import java.util.List;

public interface IUserAppDAO extends IGenericDAO<UserApp, Long> {

    List<UserApp> findByUserId(Users user);

    List<UserApp> findByUserId(Users user, String displayType);

    UserApp getByUserAndApp(Long userId, SHSApp shsApp);
}
