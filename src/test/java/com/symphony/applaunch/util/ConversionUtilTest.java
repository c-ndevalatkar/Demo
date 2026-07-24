package com.symphony.applaunch.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import com.symphony.applaunch.dto.AppLaunchDto;
import com.symphony.applaunch.dto.SHSAppDto;
import com.symphony.applaunch.entity.AppLaunch;
import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import org.junit.jupiter.api.Test;

class ConversionUtilTest {

    private final ConversionUtil conversionUtil = new ConversionUtil();

    @Test
    void testShsAppDtoToEntityAndBack() {

        Date now = new Date();

        SHSAppDto dto = new SHSAppDto();

        dto.setId(1);
        dto.setName("Test App");
        dto.setUrl("http://test.com");
        dto.setDescription("Description");
        dto.setColor("Blue");
        dto.setFrequencies(null);
        dto.setAppType(null);
        dto.setCustomAppType("CUSTOM");
        dto.setExpirationDate(now);
        dto.setIsInternal(true);
        dto.setAtmddId(10L);
        dto.setIsPinned(true);
        dto.setCreatedBy(null);
        dto.setCreateddate(now);
        dto.setInternalId("INT123");
        dto.setSupportingGroup("SUPPORT");
        dto.setStartDate(now);
        dto.setEndDate(now);
        dto.setNumViews(100L);
        dto.setWeight(10L);
        dto.setIsMigrated(true);
        dto.setAdId(10L);
        dto.setIsActive(true);
        dto.setDisplayType("CARD");
        dto.setSequence(1);
        dto.setRoleTypeCode("ROLE");
        dto.setServer("SERVER");
        dto.setLastUpdatedBy(null);
        dto.setLastUpdatedDate(now);
        dto.setSubscriptionStatus("ACTIVE");
        dto.setShortcutUrl("shortcut");
        dto.setDefaultAuthUser("defaultUser");
        dto.setDocCount(5L);
        dto.setDisplayName("Display Name");

        SHSApp entity = conversionUtil.shsAppDtoToEntity(dto);

        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getUrl(), entity.getUrl());
        assertEquals(dto.getDisplayName(), entity.getDisplayName());

        SHSAppDto convertedBack = conversionUtil.shsAppEntityToDto(entity);

        assertEquals(dto.getId(), convertedBack.getId());
        assertEquals(dto.getName(), convertedBack.getName());
        assertEquals(dto.getUrl(), convertedBack.getUrl());
        assertEquals(dto.getDescription(), convertedBack.getDescription());
        assertEquals(dto.getColor(), convertedBack.getColor());
        assertEquals(dto.getDisplayName(), convertedBack.getDisplayName());
        assertEquals(dto.getDocCount(), convertedBack.getDocCount());
        assertEquals(dto.getShortcutUrl(), convertedBack.getShortcutUrl());
    }

    @Test
    void testAppLaunchDtoToEntity() {

        Date now = new Date();

        AppLaunchDto dto = new AppLaunchDto();

        dto.setId(10L);
        dto.setToken("TOKEN123");
        dto.setCustomAppType("CUSTOM");
        dto.setUrl("http://launch.com");
        dto.setLaunchTime(now);

        Users user = new Users();
        user.setId(1L);

        dto.setUser(user);
        dto.setUserName("testuser");

        AppLaunch entity = conversionUtil.appLaunchDtoToEntity(dto);

        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getToken(), entity.getToken());
        assertEquals(dto.getCustomAppType(), entity.getCustomAppType());
        assertEquals(dto.getUrl(), entity.getUrl());
        assertEquals(dto.getLaunchTime(), entity.getLaunchTime());
        assertEquals(dto.getUser(), entity.getUser());
        assertEquals(dto.getUserName(), entity.getUserName());
    }
}
