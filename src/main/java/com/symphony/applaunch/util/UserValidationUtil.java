package com.symphony.applaunch.util;

import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.exception.ApplicationException;
import com.symphony.applaunch.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserValidationUtil {
    /*@Autowired
    IUserAppDAO userAppDAO;*/

    private static final List<String> USER_PROFILE_EDIT_ACCESS = List.of("SHSA", "EM", "GA", "EA", "AM");

    public static void validateUser(Users user) {
        if (!validateIfNoAposthrophe(user.getAdUserName())) {
            throw new ApplicationException("AD Username cannot contain '",
                    ErrorCode.INSUFFICIENT_PARAMETERS.getCodeId(), HttpStatus.BAD_REQUEST);
        }
        if (!validateIfNoAposthrophe(user.getEmail())) {
            throw new ApplicationException("Email address cannot contain '",
                    ErrorCode.INSUFFICIENT_PARAMETERS.getCodeId(), HttpStatus.BAD_REQUEST);
        }
    }

    public static boolean validateIfNoAposthrophe(String input) {
        return input != null && !input.contains("'");
    }

    public boolean isUserSameAsTokenUser(Long userId, Users user) {
        return userId != null && user != null && userId.equals(user.getId());
    }

    /*public boolean isAppAssignedToUser(Users user, int appId) {
        List<SHSApp> userApps= userAppDAO.listSubscribedApps(user);
        return user != null && !userApps.stream().filter(app -> app.getId() == appId).toList().isEmpty();
    }*/

    public boolean isUserSameAsTokenUser(String adUserName, Users user) {
        return adUserName != null && user != null
                && adUserName.equalsIgnoreCase(user.getAdUserName());
    }

    public boolean canUpdateUser(Users loggedInUser) {
        return loggedInUser.getIsInternal() && USER_PROFILE_EDIT_ACCESS.contains(loggedInUser.getRole().getTypeCode());
    }
}
