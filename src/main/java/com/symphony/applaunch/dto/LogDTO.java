package com.symphony.applaunch.dto;

import lombok.*;

import java.util.Date;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {

    private Long id;

    private LogEventTypeDTO logEventType;

    private Long userId;

    private String adUserName;

    private String firstName;

    private String lastName;

    private String message;

    private Date loggedDate;

    private String ipAddress;

    private Long refId;

    private String refEntity;

    private String browserType;

    private String browserVersion;

    private Long companyId;

    private String companyName;
}
