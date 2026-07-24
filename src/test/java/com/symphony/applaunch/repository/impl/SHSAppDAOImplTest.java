package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.entity.SHSApp;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SHSAppDAOImplTest {

    @Mock
    Session session;
    @Mock
    EntityManager em;
    @Mock
    Query<SHSApp> query;
    @InjectMocks
    SHSAppDAOImpl shsAppDaoImpl;

    @Test
    public void test_findAll() {
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(anyString(), eq(SHSApp.class))).thenReturn(query);
        List<SHSApp> result = shsAppDaoImpl.findAll();
        assertNotNull(result);
    }

    @Test
    public void test_findOne() {
        int appId = 123;
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.getNamedQuery(any(String.class))).thenReturn(query);
        SHSApp result = shsAppDaoImpl.findOne(appId);
        assertNull(result);
    }

    @Test
    public void test_findOne_Exception() {
        int appId = 123;
        when(em.unwrap(Session.class)).thenReturn(null);
        SHSApp result = shsAppDaoImpl.findOne(appId);
        assertNull(result);
    }
}
