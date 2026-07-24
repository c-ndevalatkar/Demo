package com.symphony.applaunch.service;

import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import java.util.List;

public interface IAppService {

     /**
     * This method should be implemented to find list of all the apps belonging to a
     * user
     *
     * @param userId
     * @return {@link List}
     */
    public List<UserApp> listUserApps(String userId);

    /**
     * This method should be implemented to return the SHS app object as per the app
     * id provided
     *
     * @param appId
     * @return
     */
    public SHSApp getAppById(String appId);

    /**
     * This method will return the random 5 shs applications.
     *
     * @return
     */
    public List<SHSApp> getRandomAppList();

    UserApp getVerifyApps(Long userId, SHSApp shsApp);

}
