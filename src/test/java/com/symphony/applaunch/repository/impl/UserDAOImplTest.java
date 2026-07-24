package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.UserRoles;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserDAOImplTest {

    @Mock
    Session session;
    @Mock
    EntityManager em;
    @Mock
    Query query;
    @Mock
    Query countQuery;
    @Mock
    Query<Users> SQlQuery;

    @Spy
    @InjectMocks
    UserDAOImpl userDaoImpl;
    @Mock
    HibernateCriteriaBuilder builder;
    @Mock
    JpaCriteriaQuery criteriaQuery;
    @Mock
    JpaRoot root;
    @Mock
    JpaRoot countRoot;
    @Mock
    JpaCriteriaQuery countCriteriaQuery;

    @Test
    public void test_findByEmail() {
        String emailId = "email";
        Users user = new Users();
        user.setEmail(emailId);
        when(em.unwrap(Session.class)).thenReturn(session);
        doReturn(SQlQuery).when(userDaoImpl).getQueryFromNamedQuery("Users.findByEmail");
        doReturn(null).when(userDaoImpl).executeUniqueResultHqlQuery(SQlQuery);
        Users result = userDaoImpl.findByEmail(emailId);
        assertNull(result);

    }

    @Test(expected = ApplicationException.class)
    public void test_findByEmail_Exception() {
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.findByEmail(null);
    }

    @Test
    public void test_findOne() {
        Long id = 78l;
        when(em.unwrap(Session.class)).thenReturn(session);
        doReturn(SQlQuery)
                .when(userDaoImpl)
                .getQueryFromNamedQuery("Users.findOne");
        when(SQlQuery.setParameter("id", id)).thenReturn(SQlQuery);

        doReturn(null).when(userDaoImpl).executeUniqueResultHqlQuery(SQlQuery);
        Users result = userDaoImpl.findOne(id);
        assertNull(result);
    }

    @Test
    public void test_saveUserData() {
        Users user = new Users();
        when(em.unwrap(Session.class)).thenReturn(session);
        Users result = userDaoImpl.saveUserData(user);
        assertNotNull(result);
    }

    @Test
    public void test_saveUserData_Exception() {
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.saveUserData(null);

    }

    @Test
    public void test_updateUserData() {
        Users user = new Users();
        when(em.unwrap(Session.class)).thenReturn(session);
        Users result = userDaoImpl.updateUserData(user);
        assertNotNull(result);
    }

    @Test
    public void test_updateUserData_Exception() {
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.updateUserData(null);

    }

    @Test
    public void test_FindAllTokenVerifiedUsers() {
        Pageable pageable = mock(Pageable.class);
        List<Users> usersList = new ArrayList<>();
        usersList.add(new Users());
        long totalCount = 1;

        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(Users.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Users.class)).thenReturn(root);
        when(builder.createQuery(Long.class)).thenReturn(countCriteriaQuery);
        when(countCriteriaQuery.from(Users.class)).thenReturn(countRoot);
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(session.createQuery(countCriteriaQuery)).thenReturn(countQuery);
        when(session.createQuery(countCriteriaQuery).getSingleResult()).thenReturn(totalCount);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(usersList);

        Page<Users> result = userDaoImpl.findAllTokenVerifiedUsers(pageable);

        assertNotNull(result);
        assertEquals(usersList, result.getContent());
        assertEquals(totalCount, result.getTotalElements());
    }


    @Test
    public void test_deleteByUserId() {
        Long userId = 78l;
        when(em.unwrap(Session.class)).thenReturn(session);
        Long result = userDaoImpl.deleteByUserId(userId);
        assertNotNull(result);

    }

    @Test
    public void test_deleteByUserId_Exception() {
        Long userId = null;
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.deleteByUserId(userId);

    }

    @Test
    public void test_deleteByUserId_E() {
        Long userId = null;
        when(em.unwrap(Session.class)).thenReturn(session);
        userDaoImpl.deleteByUserId(userId);
    }

    @Test
    public void test_findByAdName() {
        String adName = "adName";
        when(em.unwrap(Session.class)).thenReturn(session);
        doReturn(SQlQuery).when(userDaoImpl).getQueryFromNamedQuery("Users.findByAdName");
        doReturn(null).when(userDaoImpl).executeUniqueResultHqlQuery(SQlQuery);
        Users result = userDaoImpl.findByAdName(adName);
        assertNull(result);
    }

    @Test(expected = ApplicationException.class)
    public void test_findByAdName_Exception() {
        String adName = "abc";
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.findByAdName(adName);

    }

    @Test
    public void test_verifyToken() {

        String token = "180";
        when(em.unwrap(Session.class)).thenReturn(session);
        doReturn(SQlQuery)
                .when(userDaoImpl)
                .getQueryFromNamedQuery("Users.verifyToken");
        when(SQlQuery.setParameter("token", "180"))
                .thenReturn(SQlQuery);
        when(SQlQuery.setParameter(
                eq("expirationTime"),
                eq(ApplicationConstants.EXPIRATION_THREE_MIN_TOKEN)))
                .thenReturn(SQlQuery);
        doReturn(null)
                .when(userDaoImpl)
                .executeUniqueResultHqlQuery(SQlQuery);
        //when(session.createQuery(anyString(), eq(Users.class))).thenReturn(SQlQuery);
        Users result = userDaoImpl.verifyToken(token);
        assertNull(result);
    }

    @Test
    public void test_findUsersByRole() {
        UserRoles role = new UserRoles();
        when(em.unwrap(Session.class)).thenReturn(session);
        doReturn(SQlQuery).when(userDaoImpl).getQueryFromNamedQuery("Users.findUsersByRole");
        doReturn(null).when(userDaoImpl).executeUniqueResultHqlQuery(SQlQuery);
        List<Users> result = userDaoImpl.findUsersByRole(role);
        assertNotNull(result);
    }

    @Test
    public void test_getGlobalUsersByCompany() {
        Long companyId = 78l;
        String userToken = "userToken";
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(anyString(), eq(Users.class))).thenReturn(query);
        List<Users> result = userDaoImpl.getGlobalUsersByCompany(companyId, userToken);
        assertNotNull(result);

    }

    @Test
    public void test_getGlobalUsersByCompany_NotNull() {
        Long companyId = 78l;
        String userToken = " ";

        List<Users> result = userDaoImpl.getGlobalUsersByCompany(companyId, userToken);
        assertNotNull(result);

    }

    @Test
    public void test_getGlobalUsersByCompany_Exception() {
        Long companyId = 78l;
        String userToken = "userToken";
        when(em.unwrap(Session.class)).thenReturn(null);
        userDaoImpl.getGlobalUsersByCompany(companyId, userToken);

    }
}
