package com.symphony.applaunch.constants;

public class ApplicationConstants {

    public static final String AUTHORIZATION = "Authorization";

    private ApplicationConstants() {
    }

    public static final String START_DATE = "startDate";

    public static final String END_DATE = "endDate";

    public static final String EMAIL_ID = "email";
    public static final String SHS = "SHS";
    public static final String OKTA = "OKTA";
    public static final String COMPANY_AUTH_TYPE = "companyAuthType";
    public static final Integer EXPIRATION_THREE_MIN_TOKEN = 60 * 3; // 3 min.
    public static final Integer EXPIRATION_TEN_HOURS = 60 * 10 * 10; // ten hours

    public static final String ERROR_PAGE = "/sso/errorPage.jsp";

    public static final String MULTIPLE_EMAILS = "Multiple users with same email id ";

    public static final String MULTIPLE_AD_USERNAME = "Multiple users with same AD username / IP ";

    public static final String INVALID_TOKEN = "invalid.token";

    public static final String TOKEN_EXPIRED = "token.expired";

    public static final String ENGAGEMENT_MANAGER_CODE = "EM";
    public static final String AD_USER_NAME = "adUserName";
    public static final String SSO_APP_ID = "ssoAppId";

    public static final String QLIKVIEW = "QlikView";
    public static final String GENERIC = "Generic SHS Apps";
    public static final String QLIKSENSE = "QlikSense";
    public static final String MICROSTRATEGY = "MicroStrategy";
    public static final String MICROSTRATEGY_CLOUD = "MicroStrategyCloud";
    public static final String DATASTEWARD = "Datasteward";
    public static final String SUPERPHAST = "Superphast";
    public static final String TABLEAU = "tableau";
    public static final String MDT = "mdt";
    public static final String LOGINID = "loginId";
    public static final String CATCH_MESSAGE = "Catch :";
    public static final String APPTYPE = "appType";
    public static final String DISPLAY_TYPE = "displayType";

    public static final String USER_ID = "userId";
    public static final String ALL_APPS = "All apps";
    public static final String APP_ID = "appId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String SUCCESSFULLY = " successfully";
    public static final String PK = " PK: ";

    public static final String EMAILID = "emailId";
    public static final String AD_NAME = "adName";
    public static final String UNKNOWN = "unknown";
    public static final String UTF_MSG = "utf-8";

    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String TICKET_URL = "ticketUrl";
    public static final String TICKET = "ticket";
    public static final String DOT_COM = ".com/";
    public static final String REST_ERROR = "ERROR";
    public static final String REDIRECT_URL = "redirectUrl";
    public static final String APPNAME = "appName";
    public static final String QV_SPLIT_RD_URL = "/QvAJAXZfc";
    public static final String QV_RD_URL_INDEX_OF = "/QvPlugin";
    public static final String QV_RD_SPLIT_HOST = "&host=";
    public static final String QV_RD_SPLIT_DOCUMENT = "document=";
    public static final String LOG_QV_TICKET = "Ticket Url : ";
    public static final String QS_TICKET_RD_URL = "?qlikTicket=";
    public static final String TOKEN_LOG_INFO = "?token=";
    public static final String SERVER = "server";

    public static final String USER_APP_DATA_APPEND_TABLE = "select uap.id as id,uap.userId as userId,uap.shsApp as shsApp, uap.companyId as companyId,  uap.subscriptionFrequency  as subscriptionFrequency, uap.subscriptionStatus as subscriptionStatus,   uap.expirationDate as expirationDate,   uap.startDate as startDate,    uap.createdDate  as createdDate ";
    public static final String UAP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_AND_UAP_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE = " and (uap.startDate is null or uap.startDate <=:startDate) and (uap.expirationDate is null or uap.expirationDate >=:startDate) ";
    public static final String UAP_DOT_SHSAPP_DOT_STARTDATE_IS_NULL_OR_LESS_THAN_STARTDATE_AND_UAP_DOT_SHSAPP_DOT_EXPIRATIONDATE_IS_NULL_OR_GREATER_THAN_STARTDATE = " and (uap.shsApp.startDate is null or uap.shsApp.startDate <=:startDate) and (uap.shsApp.expirationDate is null or uap.shsApp.expirationDate >=:startDate) ";
    public static final String ORDER_BY_UAP_DOT_SHSAPP_DOT_NAME = " order by  uap.shsApp.name ";
    public static final String UAP_DOT_SHSAPP_DOT_APPTYPE_ISINTERNAL_IS_FALSE_OR_NULL_AND_UAP_DOT_SHSAPP_ISINTERNAL_IS_FALSE_OR_NULL = " and (uap.shsApp.appType.isInternal is false or uap.shsApp.appType.isInternal is null)  and (uap.shsApp.isInternal is false or uap.shsApp.isInternal is null) ";

    // requsst url- HTTPS
    public static final String HTTPS = "https://";
    public static final String US_EAST_REGION = "us-east-1";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized Access";
    public static final String BAD_REQUEST = "Bad Request";
    
	public static final String TOKEN_CREATION_ERROR = "Error Creating Token";
	public static final String USER_NOT_FOUND = "User does not exist";
	public static final String SERVER_URL = "serverurl";

	public static final String ERROR = "error";

	public static final String LOG_QV_TICKE = "Ticket Url : ";


	

}
