package com.symphony.applaunch.repository;

import com.symphony.applaunch.dto.PaginationVO;
import com.symphony.applaunch.entity.UserLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserLoginDAO extends IGenericDAO<UserLogin, Long> {
    /**
     * This method is used to add a new user login
     *
     * @param userLogin  json object containing user login
     * @return UserLogin response having saved user login object
     */
    public Long saveUserLogin(UserLogin userLogin);

    /**
     *
     * @return all user logins count
     */
    public Integer getAllUserLoginsCount();

    public Page<UserLogin> getUserDataList(PaginationVO paginationVO, Pageable pageable);

    List<Object[]> getUserDataListArray(PaginationVO paginationVO);

    public void updateUserLogin(String adUserName);
}
