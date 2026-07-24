package com.symphony.applaunch.repository;

import com.symphony.applaunch.exception.ApplicationException;
import jakarta.persistence.EntityManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class GenericDAOTest {

    // A minimal dummy entity
    public static class Dummy implements Serializable {
        private Long id;
        public Dummy() {}
        public Dummy(Long id) { this.id = id; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    /** We’ll test GenericDAO<Dummy,Long> via this concrete subclass. */
    public static class DummyDAO extends GenericDAO<Dummy,Long> {
        public DummyDAO() { super(Dummy.class); }
    }

    @InjectMocks
    private DummyDAO dao;

    @Mock
    private EntityManager em;

    @Mock
    private Session session;

    @Before
    public void setUp() {
        // unwrap the Hibernate session
        when(em.unwrap(Session.class)).thenReturn(session);
    }

    @Test
    public void getCrntSession_success() {
        assertSame(session, dao.getCrntSession());
    }

    @Test(expected = ApplicationException.class)
    public void getCrntSession_unwrapFails_throws() {
        when(em.unwrap(Session.class)).thenThrow(new HibernateException("no session"));
        dao.getCrntSession();
    }

    @Test
    public void save_success() {
        Dummy d = new Dummy();
        when(session.save(d)).thenReturn(99L);
        Long id = dao.save(d);
        assertEquals(Long.valueOf(99L), id);
    }

    @Test(expected = ApplicationException.class)
    public void save_hibernateFails_throws() {
        Dummy d = new Dummy();
        when(session.save(d)).thenThrow(new HibernateException("oops"));
        dao.save(d);
    }

    @Test
    public void update_success() {
        Dummy d = new Dummy();
        // should call clear() then update()
        dao.update(d);
        InOrder inOrder = inOrder(session);
        inOrder.verify(session).clear();
        inOrder.verify(session).update(d);
    }

    @Test(expected = ApplicationException.class)
    public void update_hibernateFails_throws() {
        Dummy d = new Dummy();
        // make clear() succeed but update() fail
        doNothing().when(session).clear();
        doThrow(new HibernateException("bad")).when(session).update(d);
        dao.update(d);
    }

    @Test
    public void saveEntity_success() {
        Object o = new Object();
        when(session.save(o)).thenReturn(123L);
        Long id = dao.saveEntity(o);
        assertEquals(Long.valueOf(123L), id);
    }

    @Test(expected = ApplicationException.class)
    public void saveEntity_fail() {
        Object o = new Object();
        when(session.save(o)).thenThrow(new HibernateException("fail"));
        dao.saveEntity(o);
    }

    @Test
    public void updateEntity_success() {
        Object o = new Object();
        dao.updateEntity(o);
        verify(session).update(o);
    }

    @Test(expected = ApplicationException.class)
    public void updateEntity_fail() {
        Object o = new Object();
        doThrow(new HibernateException("x")).when(session).update(o);
        dao.updateEntity(o);
    }

    @Test
    public void saveOrUpdateAll_success() {
        List<Object> list = Arrays.asList("a", "b");
        dao.saveOrUpdateAll(list);
        verify(session, times(2)).saveOrUpdate(any());
    }

    @Test(expected = ApplicationException.class)
    public void saveOrUpdateAll_fail() {
        List<Object> list = Arrays.asList("x");
        doThrow(new HibernateException("err")).when(session).saveOrUpdate("x");
        dao.saveOrUpdateAll(list);
    }

    @Test
    public void get_success() {
        Dummy d = new Dummy(5L);
        when(session.get(Dummy.class, 5L)).thenReturn(d);
        Dummy out = dao.get(5L);
        assertSame(d, out);
    }

    @Test(expected = ApplicationException.class)
    public void get_fail() {
        when(session.get(Dummy.class, 1L)).thenThrow(new HibernateException("no"));
        dao.get(1L);
    }

    @Test
    public void load_success() {
        Dummy d = new Dummy(7L);
        when(session.load(Dummy.class, 7L)).thenReturn(d);
        assertSame(d, dao.load(7L));
    }

    @Test(expected = ApplicationException.class)
    public void load_fail() {
        when(session.load(Dummy.class, 2L)).thenThrow(new HibernateException("err"));
        dao.load(2L);
    }

    @Test
    public void loadObject_success() {
        String s = "hello";
        when(session.load(String.class, 3L)).thenReturn(s);
        assertEquals("hello", dao.loadObject(String.class, 3L));
    }

    @Test(expected = ApplicationException.class)
    public void loadObject_fail() {
        when(session.load(String.class, 4L)).thenThrow(new HibernateException("err"));
        dao.loadObject(String.class, 4L);
    }

    @Test
    public void loadMasterObject_success() {
        Integer id = 8;
        when(session.load(String.class, id)).thenReturn("v8");
        assertEquals("v8", dao.loadMasterObject(String.class, id));
    }

    @Test(expected = ApplicationException.class)
    public void loadMasterObject_fail() {
        when(session.load(String.class, 9)).thenThrow(new HibernateException("no"));
        dao.loadMasterObject(String.class, 9);
    }

    @Test
    public void getObject_success() {
        when(session.get(String.class, 10L)).thenReturn("ten");
        assertEquals("ten", dao.getObject(String.class, 10L));
    }

    @Test(expected = ApplicationException.class)
    public void getObject_fail() {
        when(session.get(String.class, 11L)).thenThrow(new HibernateException("err"));
        dao.getObject(String.class, 11L);
    }

    @Test
    public void getMasterObject_success() {
        when(session.get(String.class, 12)).thenReturn("twelve");
        assertEquals("twelve", dao.getMasterObject(String.class, 12));
    }

    @Test(expected = ApplicationException.class)
    public void getMasterObject_fail() {
        when(session.get(String.class, 13)).thenThrow(new HibernateException("err"));
        dao.getMasterObject(String.class, 13);
    }

    @Test
    public void delete_success() {
        Dummy d = new Dummy();
        dao.delete(d);
        verify(session).delete(d);
    }

    @Test(expected = ApplicationException.class)
    public void delete_fail() {
        Dummy d = new Dummy();
        doThrow(new HibernateException("err")).when(session).delete(d);
        dao.delete(d);
    }

    @Test
    public void mergeSession_success() {
        Dummy d = new Dummy(99L);
        when(session.merge(d)).thenReturn(d);
        Dummy out = dao.mergeSession(d);
        assertSame(d, out);
    }

    @Test(expected = ApplicationException.class)
    public void mergeSession_fail() {
        Dummy d = new Dummy();
        when(session.merge(d)).thenThrow(new HibernateException("err"));
        dao.mergeSession(d);
    }

    @Test
    public void clearSession_success() {
        dao.clearSession();
        InOrder o = inOrder(session);
        o.verify(session).flush();
        o.verify(session).clear();
    }

    @Test(expected = ApplicationException.class)
    public void clearSession_fail() {
        doThrow(new HibernateException("x")).when(session).flush();
        dao.clearSession();
    }

    @Test
    public void saveOrUpdate_delegates() {
        Dummy d = new Dummy();
        dao.saveOrUpdate(d);
        verify(session).saveOrUpdate(d);
    }

    @Test(expected = ApplicationException.class)
    public void saveOrUpdate_fail() {
        Dummy d = new Dummy();
        doThrow(new HibernateException("x")).when(session).saveOrUpdate(d);
        dao.saveOrUpdate(d);
    }

    @Test
    public void saveOrUpdateEntity_delegates() {
        Object x = new Object();
        dao.saveOrUpdateEntity(x);
        verify(session).saveOrUpdate(x);
    }

    @Test(expected = ApplicationException.class)
    public void saveOrUpdateEntity_fail() {
        Object x = new Object();
        doThrow(new HibernateException("x")).when(session).saveOrUpdate(x);
        dao.saveOrUpdateEntity(x);
    }

    @Test
    public void getSQLQueryObj_success() {
        String sql = "select * from DUMMY where id = :id";

        @SuppressWarnings("unchecked")
        NativeQuery<Dummy> nativeQuery = mock(NativeQuery.class);

        // session.createNativeQuery(sql, Dummy.class) should return our mock
        when(session.createNativeQuery(sql, Dummy.class)).thenReturn(nativeQuery);

        NativeQuery<Dummy> result = dao.getSQLQueryObj(sql);

        assertSame(nativeQuery, result);
        verify(session, times(1)).createNativeQuery(sql, Dummy.class);
    }

    @Test(expected = ApplicationException.class)
    public void getSQLQueryObj_hibernateFails_shouldThrowApplicationException() {
        String sql = "select * from DUMMY where id = :id";

        // make createNativeQuery throw a HibernateException
        when(session.createNativeQuery(sql, Dummy.class))
                .thenThrow(new HibernateException("bad sql"));

        try {
            dao.getSQLQueryObj(sql);
        } finally {
            // still verify that we attempted to create the native query
            verify(session, times(1)).createNativeQuery(sql, Dummy.class);
        }
    }
}
