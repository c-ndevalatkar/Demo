package com.symphony.applaunch.repository.impl;


import com.symphony.applaunch.dto.PaginationVO;
import com.symphony.applaunch.entity.UserLogin;
import com.symphony.applaunch.entity.Users;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserLoginDAOImplTest {

    @Spy
    @InjectMocks
    UserLoginDAOImpl userLoginDAOImpl;

    @Mock
    EntityManager em;
    @Mock
    NativeQuery SQlQuery;
    @Mock
    private Session session;

    @Mock
    Query query;

    @Mock
    Query<UserLogin> userLoginQuery;

    @Test
    public void test_saveUserLogin() {
        when(em.unwrap(Session.class)).thenReturn(session);
        UserLogin userLogin = new UserLogin();
        userLoginDAOImpl.saveUserLogin(userLogin);
        assertNotNull(userLogin);
    }

    @Test
    public void test_getAllUserLoginsCount() {
        Long count = 2l;
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.getNamedQuery(any(String.class))).thenReturn(query);
        when(query.uniqueResult()).thenReturn(count);
        Integer totalcount = userLoginDAOImpl.getAllUserLoginsCount();
        assertNotNull(totalcount);
    }

    @Test
    public void test_getAllUserLoginsCount_Exception() {

        when(em.unwrap(Session.class)).thenReturn(session);
        userLoginDAOImpl.getAllUserLoginsCount();

    }

    @Test
    public void test_getUserDataList() {
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = sort != null ? PageRequest.of(0, 10, sort) : PageRequest.of(0, 10);
        PaginationVO paginationVo = new PaginationVO();
        Date startDate = new Date();
        paginationVo.setStartDate(startDate);
        Date endDate = new Date();
        paginationVo.setEndDate(endDate);
        String adUserName = "username";
        paginationVo.setAdUserName(adUserName);
        when(em.unwrap(Session.class)).thenReturn(session);
		/*when(session.createQuery(anyString(), eq(UserLogin.class)))
				.thenReturn(userLoginQuery);*/
        when(session.createQuery(anyString())).thenReturn(userLoginQuery);

        when(userLoginQuery.setParameter(eq("startDate"), any(Date.class)))
                .thenReturn(userLoginQuery);
        when(userLoginQuery.setParameter(eq("endDate"), any(Date.class)))
                .thenReturn(userLoginQuery);
        when(userLoginQuery.setParameter(eq("adUserName"), anyString()))
                .thenReturn(userLoginQuery);

        // 4) Stub pagination calls
        when(userLoginQuery.setFirstResult(anyInt())).thenReturn(userLoginQuery);
        when(userLoginQuery.setMaxResults(anyInt())).thenReturn(userLoginQuery);

        doReturn(Arrays.asList(new UserLogin(), new UserLogin(), new UserLogin()))
                .doReturn(Arrays.asList(new UserLogin(), new UserLogin()))
                .when(userLoginDAOImpl)
                .executeHQLSelectQuery(userLoginQuery);

        Page<UserLogin> result = userLoginDAOImpl.getUserDataList(paginationVo, pageable);
        assertNotNull(result);
    }

    @Test
    public void test_getUserDataList_Exception() {
        Pageable pageable = null;
        PaginationVO paginationVo = new PaginationVO();
        paginationVo.setStartDate(null);
        paginationVo.setEndDate(null);
        paginationVo.setAdUserName(null);
        when(em.unwrap(Session.class)).thenReturn(null);
        userLoginDAOImpl.getUserDataList(paginationVo, pageable);

    }

    @Test
    public void test_getUserDataListArray() {
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(any(String.class))).thenReturn(query);
        List<Object[]> list = userLoginDAOImpl.getUserDataListArray(new PaginationVO());
        assertNotNull(list);

    }

    @Test
    public void test_getUserDataListArray_Exception() {
        when(em.unwrap(Session.class)).thenReturn(null);
        userLoginDAOImpl.getUserDataListArray(new PaginationVO());
    }

    @Test
    public void test_updateUserLogin_Login() {
        String adUserName = "Token";
        Users user = new Users();
        user.setAdUserName(adUserName);
        when(em.unwrap(Session.class)).thenReturn(null);
        userLoginDAOImpl.updateUserLogin(adUserName);

    }

}
