package com.symphony.applaunch.repository;

import com.symphony.applaunch.entity.SHSApp;

import java.util.List;

public interface ISHSAppDAO extends IGenericDAO<SHSApp, Long> {

    List<SHSApp> findAll();

    SHSApp findOne(int appId);
}
