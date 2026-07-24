package com.symphony.applaunch.service;

import com.symphony.applaunch.entity.SHSApp;
import com.symphony.applaunch.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LaunchStrategy {
    /**
     * Execute launch and return final redirect URL (or null if the strategy already wrote to response)
     */
    void execute(Users loggedInuserEntity, String redirectUrl, SHSApp app, HttpServletRequest servletRequest, HttpServletResponse servletResponse);

}