package com.symphony.applaunch.service.impl;

import ch.qos.logback.classic.Level;
import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.UserApp;
import com.symphony.applaunch.entity.Users;
import com.symphony.applaunch.repository.ISHSAppDAO;
import com.symphony.applaunch.repository.IUserAppDAO;
import com.symphony.applaunch.repository.IUserDAO;
import com.symphony.applaunch.service.IAppService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;

@RequiredArgsConstructor
@Component("appService")
public class AppServiceImpl implements IAppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    private final Random random = new SecureRandom();
    private final IUserDAO usersDAO;
    private final ISHSAppDAO shsAppDAO;
    private final IUserAppDAO userAppDAO;

    /**
     * This method should be implemented to find list of all the apps belonging to a
     * user
     *
     * @param userId
     * @return {@link List}
     */
    @Override
    public List<UserApp> listUserApps(String userId) {
        try {
            Users user = usersDAO.findOne(Long.parseLong(userId));
            return userAppDAO.findByUserId(user);
        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE +"{}", e);
        }
        return new ArrayList<>();
    }

    @Override
    public UserApp getVerifyApps(Long userId, SHSApp shsApp) {

        return userAppDAO.getByUserAndApp(userId, shsApp);
    }

    /**
     * This method should be implemented to return the SHS app object as per the app
     * id provided
     *
     * @param appId
     * @return
     */
    @Override
    public SHSApp getAppById(String appId) {
        try {
            logger.info(String.valueOf(Level.INFO), "appId = ", appId);
            return shsAppDAO.findOne(Integer.parseInt(appId));
        } catch (Exception e) {
            logger.error("Exception occured while fetching app using app id : " + e);
        }
        return null;
    }

    /**
     * This method will return the random 5 shs applications.
     *
     * @return
     */
    @Override
    public List<SHSApp> getRandomAppList() {
        List<SHSApp> randomSHSAppList = new ArrayList<>();

        List<SHSApp> shsAppList = shsAppDAO.findAll();
        while (randomSHSAppList.size() < 5) {

            int min = 0;
            int max = shsAppList.size();
            int randomNum = random.nextInt(max - min) + min;
            randomSHSAppList.add(shsAppList.get(randomNum));
        }

        return randomSHSAppList;
    }

}
