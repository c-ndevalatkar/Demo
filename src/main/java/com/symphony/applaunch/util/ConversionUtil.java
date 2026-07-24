package com.symphony.applaunch.util;

import org.springframework.stereotype.Component;

import com.symphony.applaunch.dto.AppLaunchDto;
import com.symphony.applaunch.dto.SHSAppDto;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.entity.SHSApp;

@Component
public class ConversionUtil {

	public SHSApp shsAppDtoToEntity(SHSAppDto shsappdto) {

		SHSApp entity = new SHSApp();

		entity.setId(shsappdto.getId());
		entity.setName(shsappdto.getName());
		entity.setUrl(shsappdto.getUrl());
		entity.setDescription(shsappdto.getDescription());
		entity.setColor(shsappdto.getColor());
		entity.setFrequencies(shsappdto.getFrequencies());
		entity.setAppType(shsappdto.getAppType());
		entity.setCustomAppType(shsappdto.getCustomAppType());
		entity.setExpirationDate(shsappdto.getExpirationDate());
		entity.setIsInternal(shsappdto.getIsInternal());
		entity.setAtmddId(shsappdto.getAtmddId());
		entity.setIsPinned(shsappdto.getIsPinned());
		entity.setCreatedBy(shsappdto.getCreatedBy());
		entity.setCreateddate(shsappdto.getCreateddate());
		entity.setInternalId(shsappdto.getInternalId());
		entity.setSupportingGroup(shsappdto.getSupportingGroup());
		entity.setStartDate(shsappdto.getStartDate());
		entity.setEndDate(shsappdto.getEndDate());
		entity.setNumViews(shsappdto.getNumViews());
		entity.setWeight(shsappdto.getWeight());
		entity.setIsMigrated(shsappdto.getIsMigrated());
		entity.setAdId(shsappdto.getAdId());
		entity.setIsActive(shsappdto.getIsActive());
		entity.setDisplayType(shsappdto.getDisplayType());
		entity.setSequence(shsappdto.getSequence());
		entity.setRoleTypeCode(shsappdto.getRoleTypeCode());
		entity.setServer(shsappdto.getServer());
		entity.setLastUpdatedBy(shsappdto.getLastUpdatedBy());
		entity.setLastUpdatedDate(shsappdto.getLastUpdatedDate());
		entity.setSubscriptionStatus(shsappdto.getSubscriptionStatus());
		entity.setShortcutUrl(shsappdto.getShortcutUrl());
		entity.setDefaultAuthUser(shsappdto.getDefaultAuthUser());
		entity.setDocCount(shsappdto.getDocCount());
		entity.setDisplayName(shsappdto.getDisplayName());
		return entity;

	}

	public AppLaunch appLaunchDtoToEntity(AppLaunchDto appLaunchDto) {

		AppLaunch toEntity = new AppLaunch();

		toEntity.setId(appLaunchDto.getId());
		toEntity.setToken(appLaunchDto.getToken());
		toEntity.setCustomAppType(appLaunchDto.getCustomAppType());
		toEntity.setUrl(appLaunchDto.getUrl());
		toEntity.setLaunchTime(appLaunchDto.getLaunchTime());
		toEntity.setUser(appLaunchDto.getUser());
		toEntity.setUserName(appLaunchDto.getUserName());

		return toEntity;
	}

	// Method to convert from Entity to DTO(SHSAPP)
	public SHSAppDto shsAppEntityToDto(SHSApp shsappEntity) {

		SHSAppDto shsAppDto = new SHSAppDto();

		shsAppDto.setId(shsappEntity.getId());
		shsAppDto.setName(shsappEntity.getName());
		shsAppDto.setUrl(shsappEntity.getUrl());
		shsAppDto.setDescription(shsappEntity.getDescription());
		shsAppDto.setColor(shsappEntity.getColor());
		shsAppDto.setFrequencies(shsappEntity.getFrequencies());
		shsAppDto.setAppType(shsappEntity.getAppType());
		shsAppDto.setCustomAppType(shsappEntity.getCustomAppType());
		shsAppDto.setExpirationDate(shsappEntity.getExpirationDate());
		shsAppDto.setIsInternal(shsappEntity.getIsInternal());
		shsAppDto.setAtmddId(shsappEntity.getAtmddId());
		shsAppDto.setIsPinned(shsappEntity.getIsPinned());
		shsAppDto.setCreatedBy(shsappEntity.getCreatedBy());
		shsAppDto.setCreateddate(shsappEntity.getCreateddate());
		shsAppDto.setInternalId(shsappEntity.getInternalId());
		shsAppDto.setSupportingGroup(shsappEntity.getSupportingGroup());
		shsAppDto.setStartDate(shsappEntity.getStartDate());
		shsAppDto.setEndDate(shsappEntity.getEndDate());
		shsAppDto.setNumViews(shsappEntity.getNumViews());
		shsAppDto.setWeight(shsappEntity.getWeight());
		shsAppDto.setIsMigrated(shsappEntity.getIsMigrated());
		shsAppDto.setAdId(shsappEntity.getAdId());
		shsAppDto.setIsActive(shsappEntity.getIsActive());
		shsAppDto.setDisplayType(shsappEntity.getDisplayType());
		shsAppDto.setSequence(shsappEntity.getSequence());
		shsAppDto.setRoleTypeCode(shsappEntity.getRoleTypeCode());
		shsAppDto.setServer(shsappEntity.getServer());
		shsAppDto.setLastUpdatedBy(shsappEntity.getLastUpdatedBy());
		shsAppDto.setLastUpdatedDate(shsappEntity.getLastUpdatedDate());
		shsAppDto.setSubscriptionStatus(shsappEntity.getSubscriptionStatus());
		shsAppDto.setShortcutUrl(shsappEntity.getShortcutUrl());
		shsAppDto.setDefaultAuthUser(shsappEntity.getDefaultAuthUser());
		shsAppDto.setDocCount(shsappEntity.getDocCount());
		shsAppDto.setDisplayName(shsappEntity.getDisplayName());

		return shsAppDto;
	}

}
