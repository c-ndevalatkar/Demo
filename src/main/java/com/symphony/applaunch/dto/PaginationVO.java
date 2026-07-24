package com.symphony.applaunch.dto;

import com.symphony.applaunch.entity.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class PaginationVO {

    private Long page;
    private Long recordsPerPage;
    private Long companyId;
    private String orderByColumn;
    private String sortOrder;
    private String firstName;
    private String lastName;
    private String company;
    private Company companyObj;
    private String glossary;
    private String companyType;
    private String userType;
    private String appId;
    private String role;
    private Boolean isInternal;
    private Users engagementManager;
    private String email;
    private String adUserName;
    private Date startDate;
    private Date endDate;
    private String menu;
    private String name;
    private String color;
    private String description;
    private String shsAppName;
    private SHSApp app;
    private String appname;
    private String documentId;
    private String lastUpdatedBy;
    private AppType apptype;
    private List<Long> userIds;
    private String financeCode;
    private Integer startIndex;
    private String customType;
    private String type;
    private Long userId;
    private String title;
    private String messageType;
    private Users user;
    private Integer recipient;
    private String displayType;
    private String url;
    private Long marketID;
    private String marketName;
    private UserRoles userRoles;
    private Menu menuObj;
    private String fileName;
    private Long dimensionId;
    private List<Company> companylist;
    private String lead;
    private String member;
    private String subject;
    private Boolean formFlag;
    private String solution;
    private String comment;
    private String formNo;
    private String status;
    private Integer netLikes;
    private String generalSearch;
    private List<ActiveDay> daysActive;
    private Date startDate1;
    private Date endDate1;
    private String message1;
    private String alertType;
    private String query;
    private Map<String, Object> filters;
    private Long reportId;
    private String reportName;
    private LocalDateTime bannerStartDate;
    private LocalDateTime bannerEndDate;
    private String logUser;
    private String loggedInUserRole;

}
