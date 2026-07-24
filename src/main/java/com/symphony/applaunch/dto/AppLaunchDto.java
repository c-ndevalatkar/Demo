package com.symphony.applaunch.dto;

import java.util.Date;

import com.symphony.applaunch.entity.Users;

public class AppLaunchDto {

	public AppLaunchDto() {
		super();
	}

	private Long id;

	private String token;

	private String customAppType;

	private String url;

	private Date launchTime;

	private Users user;

	private String userName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCustomAppType() {
		return customAppType;
	}

	public void setCustomAppType(String customAppType) {
		this.customAppType = customAppType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getLaunchTime() {
		return launchTime;
	}

	public void setLaunchTime(Date launchTime) {
		this.launchTime = launchTime;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
