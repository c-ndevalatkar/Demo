package com.symphony.applaunch.dto;

import java.util.Date;
import java.util.List;

import com.symphony.applaunch.entity.AppType;
import com.symphony.applaunch.entity.Frequencies;
import com.symphony.applaunch.entity.Users;

public class SHSAppDto {

	private Integer id;

	private String name;

	private String url;

	private String description;

	private String color;

	private List<Frequencies> frequencies;

	private AppType appType;

	private String customAppType;

	private Date expirationDate;

	private Boolean isInternal = false;

	private Long atmddId;

	private Boolean isPinned;

	private Date createddate = new Date();

	private Users createdBy;

	private String internalId;

	private String supportingGroup;

	private Date startDate;

	private Date endDate;

	private Long numViews;

	private Long weight;

	private Boolean isMigrated = false;

	private Long adId;

	private Boolean isActive;

	private String displayType = "AP";

	private Integer sequence = 0;

	private String roleTypeCode;

	private String server;

	private Users lastUpdatedBy;

	private Date lastUpdatedDate;

	private String subscriptionStatus;

	private String shortcutUrl;

	private String defaultAuthUser;

	private String displayName;

	private Long docCount;

	private String iconKey;

	private String iconUrl;

	public String getIconKey() {
		return iconKey;
	}

	public void setIconKey(String iconKey) {
		this.iconKey = iconKey;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Boolean getIsPinned() {
		return isPinned;
	}

	public void setIsPinned(Boolean isPinned) {
		this.isPinned = isPinned;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Date getCreateddate() {
		return createddate;
	}

	public void setCreateddate(Date createddate) {
		this.createddate = createddate;
	}

	public Users getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Users createdBy) {
		this.createdBy = createdBy;
	}

	public Long getAtmddId() {
		return atmddId;
	}

	public void setAtmddId(Long atmddId) {
		this.atmddId = atmddId;
	}

	public Long getDocCount() {
		return docCount;
	}

	public void setDocCount(Long docCount) {
		this.docCount = docCount;
	}

	public Boolean getIsInternal() {
		return isInternal;
	}

	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public List<Frequencies> getFrequencies() {
		return frequencies;
	}

	public void setFrequencies(List<Frequencies> frequencies) {
		this.frequencies = frequencies;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public String getCustomAppType() {
		return customAppType;
	}

	public void setCustomAppType(String customAppType) {
		this.customAppType = customAppType;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public String getSupportingGroup() {
		return supportingGroup;
	}

	public void setSupportingGroup(String supportingGroup) {
		this.supportingGroup = supportingGroup;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getNumViews() {
		return numViews;
	}

	public void setNumViews(Long numViews) {
		this.numViews = numViews;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsMigrated() {
		return isMigrated;
	}

	public void setIsMigrated(Boolean isMigrated) {
		this.isMigrated = isMigrated;
	}

	public Long getAdId() {
		return adId;
	}

	public void setAdId(Long adId) {
		this.adId = adId;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getRoleTypeCode() {
		return roleTypeCode;
	}

	public void setRoleTypeCode(String roleTypeCode) {
		this.roleTypeCode = roleTypeCode;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public Users getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Users lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getShortcutUrl() {
		return shortcutUrl;
	}

	public void setShortcutUrl(String shortcutUrl) {
		this.shortcutUrl = shortcutUrl;
	}

	public String getDefaultAuthUser() {
		return defaultAuthUser;
	}

	public void setDefaultAuthUser(String defaultAuthUser) {
		this.defaultAuthUser = defaultAuthUser;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
