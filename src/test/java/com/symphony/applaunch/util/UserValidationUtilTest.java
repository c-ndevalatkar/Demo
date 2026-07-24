package com.symphony.applaunch.util;


import com.symphony.applaunch.entity.UserRoles;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.repository.IUserAppDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class UserValidationUtilTest {

    @InjectMocks
    UserValidationUtil userValidation;

    @Mock
    IUserAppDAO userAppDAO;

    @Test
    public void test_canUpdateUser() {
        Users user = new Users();
        user.setIsInternal(true);

        UserRoles role = new UserRoles();
        role.setTypeCode("SHSA");
        user.setRole(role);
        assertTrue(userValidation.canUpdateUser(user));

        role.setTypeCode("End User");
        user.setRole(role);
        assertFalse(userValidation.canUpdateUser(user));

        user.setIsInternal(false);
        assertFalse(userValidation.canUpdateUser(user));
    }

    @Test
    public void test_isUserSameAsTokenUser() {
        Users user = new Users();
        user.setAdUserName("test");
        assertTrue(userValidation.isUserSameAsTokenUser("test", user));

        assertFalse(userValidation.isUserSameAsTokenUser("Test", null));

        assertFalse(userValidation.isUserSameAsTokenUser("Testad", user));
    }

    @Test
    public void test_isUserSameAsTokenUser_2() {
        Users user = new Users();
        user.setId(1l);
        assertTrue(userValidation.isUserSameAsTokenUser(1l, user));

        assertFalse(userValidation.isUserSameAsTokenUser(1l, null));

        assertFalse(userValidation.isUserSameAsTokenUser(2l, user));
    }


    @SuppressWarnings("static-access")
    @Test
    public void test_validateIfNoAposthrophe() {
        assertFalse(userValidation.validateIfNoAposthrophe(null));
        assertTrue(userValidation.validateIfNoAposthrophe("test"));
        assertFalse(userValidation.validateIfNoAposthrophe("test'"));
    }

    @SuppressWarnings("static-access")
    @Test
    public void test_validateUser() {
        Users user = new Users();
        assertThrows(ApplicationException.class, () -> userValidation.validateUser(user));

        user.setAdUserName("test");
        assertThrows(ApplicationException.class, () -> userValidation.validateUser(user));

        user.setEmail("test");
        userValidation.validateUser(user);
        assertTrue(true);
    }

}
