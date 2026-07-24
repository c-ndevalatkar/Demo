package com.symphony.applaunch.repository;

import java.util.List;

import com.symphony.applaunch.entity.AppLaunch;

public interface IAppLaunchDao {
	AppLaunch saveAppLaunch(AppLaunch appLaunch);

	List<AppLaunch> getCurrentAppLaunch(String username);
}
