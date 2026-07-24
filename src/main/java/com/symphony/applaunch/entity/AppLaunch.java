package com.symphony.applaunch.entity;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "app_launch")
public class AppLaunch {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_launch_id_seq")
	@SequenceGenerator(name = "app_launch_id_seq", sequenceName = "app_launch_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@Column(name = "token")
	private String token;

	@Column(name = "custom_app_type")
	private String customAppType;

	@Column(name = "URL")
	private String url;

	@Column(name = "launch_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date launchTime;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password" })
	private Users user;

	@Transient
	private String userName;

	public AppLaunch() {
		super();
	}

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
