package com.symphony.applaunch.dto;

import com.symphony.applaunch.entity.Menu;
import com.symphony.applaunch.entity.Users;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VerificationTokenDTO {

    boolean isTokenExpired;

    String email;

    String adUserName;

    String companyName;

    boolean isAuthenticated;

    String message;

    Users user;

    private Integer appId;

    private String url;

    private String appName;

    List<Menu> authorizedFeatures;

    List<Users> usersList;

    public void setIsTokenExpired(boolean isTokenExpired) {
        this.isTokenExpired = isTokenExpired;
    }

    public boolean getIsTokenExpired() {
        return this.isTokenExpired;
    }
}
