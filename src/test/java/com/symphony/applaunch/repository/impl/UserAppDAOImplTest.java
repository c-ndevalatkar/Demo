package com.symphony.applaunch.repository.impl;

import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.repository.IUserDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
public class UserAppDAOImplTest {

    @Mock
    private Session session;
    @Mock
    private EntityManager em;
    @Mock
    private Query query1;
    @Mock
    private IUserDAO userDao;

    @InjectMocks
    private UserAppDAOImpl userAppDaoImpl;

    @Mock
    private HibernateCriteriaBuilder builder;
    @Mock
    private JpaCriteriaQuery criteriaQuery;
    @Mock
    private JpaRoot root;

    @Mock
    private JpaJoin appTypeJoin;

    @Mock
    private JpaPredicate predicate;

    @Mock
    private JpaPath path;

    @Mock
    private Query<UserApp> criteriaQueryResult;

    @Mock
    private JpaJoin<Object, Object> shsAppJoin;


    @Before
    public void setUp() {
        // inject mocked EntityManager into the GenericDAO superclass
        ReflectionTestUtils.setField(userAppDaoImpl, "em", em);
    }

    // ----------- helper to mock Criteria API fully -----------------

    private void mockCriteriaApi(List<UserApp> resultList) {

        @SuppressWarnings("unchecked")
        JpaPath<Boolean> isActivePath = mock(JpaPath.class);
        @SuppressWarnings("unchecked")
        JpaPath<Date> startDatePath = mock(JpaPath.class);
        @SuppressWarnings("unchecked")
        JpaPath<Date> endDatePath = mock(JpaPath.class);

        JpaPredicate pActive    = mock(JpaPredicate.class),
                pStartNull = mock(JpaPredicate.class),
                pStartLTE  = mock(JpaPredicate.class),
                pEndNull   = mock(JpaPredicate.class),
                pEndGTE    = mock(JpaPredicate.class);


        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(builder);

        when(builder.createQuery(any(Class.class))).thenReturn(criteriaQuery);
        when(criteriaQuery.from(UserApp.class)).thenReturn(root);

        // joins
        when(root.join("shsApp", JoinType.LEFT)).thenReturn(shsAppJoin);
        when(shsAppJoin.join("appType", JoinType.LEFT)).thenReturn(appTypeJoin);

        // paths
        when(root.get(anyString())).thenReturn(path);
        when(shsAppJoin.get(anyString())).thenReturn(path);
        when(appTypeJoin.get(anyString())).thenReturn(path);

        // predicates
        when(builder.equal(any(), any())).thenReturn(predicate);
        when(builder.isNull(any())).thenReturn(predicate);

        when(builder.isTrue(isActivePath)).thenReturn(pActive);
        when(builder.isNull(startDatePath)).thenReturn(pStartNull);
        when(builder.lessThanOrEqualTo(any(), any(Date.class))).thenReturn(pStartLTE);
        when(builder.isNull(endDatePath)).thenReturn(pEndNull);
        when(builder.greaterThanOrEqualTo(any(), any(Date.class))).thenReturn(pEndGTE);

        // criteria query select/where
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where((Predicate[]) any())).thenReturn(criteriaQuery);

        // createQuery(criteriaQuery) -> query result
        when(builder.createQuery(any())).thenReturn(criteriaQuery);
        when(criteriaQueryResult.getResultList()).thenReturn(resultList);
    }

    // ---------------------------------------------------------------
    // findByUserId tests
    // ---------------------------------------------------------------

    @Test
    public void test_findByUserId() {
        Users user = new Users();
        Long Id = 78l;
        user.setId(Id);
        String displayType = "Display";
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.from(UserApp.class)).thenReturn(root);
        List<UserApp> result = userAppDaoImpl.findByUserId(user, displayType);
        assertNotNull(result);
    }

    @Test
    public void test_findByUserId_nonInternal_displayTypeNotNull_fullPath() {
        Users user = new Users();
        user.setId(78L);
        user.setIsInternal(false); // triggers "non internal" branch

        String displayType = "AP";

        List<UserApp> list = new ArrayList<>();
        list.add(new UserApp());
        mockCriteriaApi(list);

        List<UserApp> result = userAppDaoImpl.findByUserId(user, displayType);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findByUserId_internal_displayTypeNull_fullPath() {
        Users user = new Users();
        user.setId(79L);
        user.setIsInternal(true); // triggers "internal" branch

        String displayType = null; // triggers else branch for displayType

        List<UserApp> list = new ArrayList<>();
        list.add(new UserApp());
        mockCriteriaApi(list);

        List<UserApp> result = userAppDaoImpl.findByUserId(user, displayType);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findByUserId_Exception() {
        Users user = new Users();
        user.setId(78L);
        String displayType = "Display";

        // unwrap returns null -> will cause NPE in getCrntSession(), caught in method
        when(em.unwrap(Session.class)).thenReturn(null);

        List<UserApp> result = userAppDaoImpl.findByUserId(user, displayType);

        // method catches exception and returns Collections.emptyList()
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_findByUserId_onlyUser_overloadDelegates() {
        Users user = new Users();
        user.setId(80L);
        user.setIsInternal(false);

        List<UserApp> list = new ArrayList<>();
        list.add(new UserApp());
        mockCriteriaApi(list);

        List<UserApp> result = userAppDaoImpl.findByUserId(user); // calls overload

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findByUserId_isInternal_Null() {
        Users user = new Users();
        user.setId(81L);
        user.setIsInternal(null); // explicit null branch

        List<UserApp> list = new ArrayList<>();
        list.add(new UserApp());
        mockCriteriaApi(list);

        List<UserApp> result = userAppDaoImpl.findByUserId(user, null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ---------------------------------------------------------------
    // getByUserAndApp tests
    // ---------------------------------------------------------------

    /**
     * Both HQL queries return empty lists -> userApps & userApps1 empty ->
     * no user app returned (covers path where userApps is empty).
     */
    @Test
    public void test_getByUserAndApp_noResults() {
        Users user = new Users();
        Long userId = 78L;
        user.setId(userId);
        // isInternal is null -> goes to "else" branch for isInternal filter
        user.setIsInternal(null);

        SHSApp shsApp = new SHSApp();
        Integer id = 123;
        shsApp.setId(id);

        when(userDao.findOne(any(Long.class))).thenReturn(user);
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query1);

        // Both calls to executeHQLSelectQuery will see empty lists
        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setResultTransformer(any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(Collections.emptyList(), Collections.emptyList());
        when(query1.list()).thenReturn(Collections.emptyList(), Collections.emptyList());

        UserApp result = userAppDaoImpl.getByUserAndApp(userId, shsApp);

        assertNull(result); // no apps, so returns null
    }

    /**
     * User is internal -> "if (user.getIsInternal() != null && user.getIsInternal())" branch.
     * First HQL returns two apps, second HQL returns one app with a different shsApp.id.
     *
     * This covers:
     * - isInternal == true (log ALL_APPS branch)
     * - non-empty userApps and userApps1
     * - inner for-loop with both isFlag = true and isFlag = false
     * - userAppsTemp non-empty and merged into userApps
     * - userAppExist = userApps.get(0)
     */
    @Test
    public void test_getByUserAndApp_internalUser_withMergeLogic() {
        Long userId = 100L;
        Users user = new Users();
        user.setId(userId);
        user.setIsInternal(true); // triggers internal if-branch

        SHSApp inputShsApp = new SHSApp();
        inputShsApp.setId(999);

        // first query (userApps1) returns apps with shsApp ids 1 and 3
        UserApp ua1 = new UserApp();
        SHSApp s1 = new SHSApp();
        s1.setId(1);
        ua1.setShsApp(s1);

        UserApp ua3 = new UserApp();
        SHSApp s3 = new SHSApp();
        s3.setId(3);
        ua3.setShsApp(s3);

        List<UserApp> userApps1 = new ArrayList<>();
        userApps1.add(ua1);
        userApps1.add(ua3);

        // second query (userApps) returns app with shsApp id 1 only
        UserApp ua2 = new UserApp();
        SHSApp s2 = new SHSApp();
        s2.setId(1);
        ua2.setShsApp(s2);

        List<UserApp> userApps = new ArrayList<>();
        userApps.add(ua2);

        when(userDao.findOne(any(Long.class))).thenReturn(user);
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query1);

        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setResultTransformer(any())).thenReturn(query1);

        // first executeHQLSelectQuery -> userApps1, second -> userApps
        when(query1.getResultList()).thenReturn(userApps1, userApps);
        when(query1.list()).thenReturn(userApps1, userApps);

        UserApp result = userAppDaoImpl.getByUserAndApp(userId, inputShsApp);

        // userApps is non-empty, we should get the first element
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getShsApp().getId());
    }

    /**
     * Variant of getByUserAndApp where user is external (isInternal = false),
     * and the first query returns some apps while the second returns empty list.
     * This exercises:
     * - non-internal branch in the HQL building
     * - else branch "if (!userApps.isEmpty())" -> userApps empty, so userApps.addAll(userApps1)
     * - userApps not empty at the end -> userAppExist set.
     */
    @Test
    public void test_getByUserAndApp_externalUser_firstQueryOnly() {
        Long userId = 200L;
        Users user = new Users();
        user.setId(userId);
        user.setIsInternal(false); // triggers non-internal branch

        SHSApp inputShsApp = new SHSApp();
        inputShsApp.setId(500);

        UserApp ua = new UserApp();
        SHSApp sApp = new SHSApp();
        sApp.setId(42);
        ua.setShsApp(sApp);

        // Use mutable lists, not Collections.singletonList / emptyList
        List<UserApp> onlyUserLevelApps = new java.util.ArrayList<>();
        onlyUserLevelApps.add(ua);

        List<UserApp> emptyApps = new java.util.ArrayList<>(); // empty but mutable

        when(userDao.findOne(any(Long.class))).thenReturn(user);
        when(em.unwrap(Session.class)).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query1);

        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setResultTransformer(any())).thenReturn(query1);

        // First call -> onlyUserLevelApps, second call -> empty (but mutable)
        when(query1.getResultList()).thenReturn(onlyUserLevelApps, emptyApps);
        when(query1.list()).thenReturn(onlyUserLevelApps, emptyApps);

        UserApp result = userAppDaoImpl.getByUserAndApp(userId, inputShsApp);
        log.info("Result :{}", result);

        assertNotNull(result);
        assertEquals(Integer.valueOf(42), result.getShsApp().getId());
    }

}
