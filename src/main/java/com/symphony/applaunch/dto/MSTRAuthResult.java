package com.symphony.applaunch.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class MSTRAuthResult {

    private final boolean success;
    private final String mstrAuthToken;      // can be null
    private final List<String> setCookies;   // list of Set-Cookie header values
    private final String verifiedUser;       // username from protected endpoint (if available)
    private final String rawStatus;          // optional: status text

    public MSTRAuthResult(boolean success, String mstrAuthToken, List<String> setCookies, String verifiedUser, String rawStatus) {
        this.success = success;
        this.mstrAuthToken = mstrAuthToken;
        this.setCookies = setCookies;
        this.verifiedUser = verifiedUser;
        this.rawStatus = rawStatus;
    }
}
