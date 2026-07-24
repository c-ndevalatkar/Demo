package com.symphony.applaunch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.applaunch.constants.ApplicationConstants;
import com.symphony.applaunch.dto.MSTRAuthResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MstrAuthService {

    @Value("${mstr.api.loginPath}")
    private String mstrApiLoginPath;

    @Value("${mstr.api.logoutPath}")
    private String mstrApiLogoutPath;

    @Value("${mstr.login.mode}")
    private Integer mstrLoginMode;

    private final CloseableHttpClient httpClient;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MstrAuthService.class);

    public MstrAuthService(CloseableHttpClient httpClient, Environment environment) {
        this.httpClient = httpClient;
        this.environment = environment;
    }

    /**
     * Performs login to MSTR, verifies session, and optionally logs out.
     *
     * @param username
     * @param password
     * @param keepSession
     * @return
     */
    public MSTRAuthResult loginAndVerify(String redirectUrl, String username, String password, boolean keepSession) {
        // Per-call cookie store & context (so multiple threads don’t share cookies)
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        // Per-request timeout config (overrides defaults if needed)
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))              // NOSONAR
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))    // time to get a connection from pool
                .setResponseTimeout(Timeout.ofSeconds(30))            // time waiting for response
                .build();

        try {
            logger.info("MSTR Action URL : " + redirectUrl);
            if (redirectUrl == null || redirectUrl.isBlank()) {
                throw new IllegalArgumentException("Redirect URL missing");
            }
            URL url = new URL(redirectUrl);
            String baseUrl = url.getProtocol() + "://" + url.getHost()
                    + (url.getPort() == -1 ? "" : (":" + url.getPort()));

            // === Step 1: POST /auth/login ===
            HttpPost loginPost = new HttpPost(baseUrl + mstrApiLoginPath);
            loginPost.setConfig(requestConfig);
            loginPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

            String loginJson = String.format(
                    "{\"username\":\"%s\", \"password\":\"%s\", \"loginMode\":%d}",
                    username, password, mstrLoginMode
            );
            loginPost.setEntity(new StringEntity(loginJson, ContentType.APPLICATION_JSON));

            String mstrAuthToken = null;
            List<String> cookieStrings;
            String verifiedUser = null;

            try (CloseableHttpResponse resp = httpClient.execute(loginPost, context)) { // NOSONAR
                int status = resp.getCode();

                if (status == 401) {
                    logger.info("Authentication failed: invalid username or password: {}", status);
                    return new MSTRAuthResult(false, null, null, null,
                            "Authentication failed: invalid username or password: " + status);
                }

                if (status != 204 && (status < 200 || status >= 300)) {
                    logger.info("Login failed: {}", status);
                    return new MSTRAuthResult(false, null, null, null,
                            "Login failed: " + status);
                }

                // 204 means success; read headers
                Header tokenHeader = resp.getFirstHeader("X-MSTR-AuthToken");
                mstrAuthToken = tokenHeader != null ? tokenHeader.getValue() : null;

                // Collect cookies from the cookie store
                cookieStrings = new ArrayList<>();
                for (Cookie c : cookieStore.getCookies()) {
                    cookieStrings.add(c.getName() + "=" + c.getValue());
                }
            }


            // === Step 2: POST /auth/logout (optional) ===
            if (!keepSession && mstrAuthToken != null) {
                HttpPost logoutPost = new HttpPost(baseUrl + mstrApiLogoutPath);
                logoutPost.setConfig(requestConfig);
                logoutPost.setHeader("X-MSTR-AuthToken", mstrAuthToken);
                logoutPost.setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));

                try (CloseableHttpResponse loResp = httpClient.execute(logoutPost, context)) { // NOSONAR
                    EntityUtils.consumeQuietly(loResp.getEntity());
                } catch (Exception logoutEx) {
                    logger.warn("Exception during MSTR logout (will be ignored): {}", logoutEx.toString(), logoutEx);
                }
            }

            logger.info("Authentication successful for user '{}'", username);
            return new MSTRAuthResult(true, mstrAuthToken, cookieStrings, verifiedUser, "OK");

        } catch (Exception e) {
            logger.error("Error calling MSTR login/verify for user '{}': {}", username, e.toString(), e);
            return new MSTRAuthResult(false, null, null, null,
                    "Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /**
     * Method to launch the MSTR Cloud application
     * @param username Mster login user name
     * @param password Mster login user pwd
     * @param request MSTR API request
     * @param response MSTR API response
     * @throws Exception throw exception
     */

    public void loginAndLaunch(String username, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String raw = request.getParameter(ApplicationConstants.REDIRECT_URL);
        logger.info("MSTR Action URL : " + raw);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Redirect URL missing");
        }

        UriComponents rawDecoded = UriComponentsBuilder
                .fromUriString(raw)
                .build()
                .encode();

        URI uri = rawDecoded.toUri();

        // Base action URL without query
        String actionUrl =
                uri.getScheme() + "://" + uri.getHost()
                        + (uri.getPort() == -1 ? "" : (":" + uri.getPort()))
                        + uri.getPath();

        // Query params (decoded for you)
        var qp = rawDecoded.getQueryParams();
        String evt = qp.getFirst("evt");
        String src = qp.getFirst("src");
        String server = qp.getFirst("Server");
        String project = qp.getFirst("Project");   // "Account Metrics"
        String port = qp.getFirst("Port");


        // Normalize project (convert + → space)
        project = Optional.ofNullable(project).orElse("").replace("+", " ");

        // Now safely escape (null-safe)
        String escUser = HtmlUtils.htmlEscape(Optional.ofNullable(username).orElse(""));
        String escPwd = HtmlUtils.htmlEscape(Optional.ofNullable(password).orElse(""));
        String pr = HtmlUtils.htmlEscape(project);
        String sv = HtmlUtils.htmlEscape(Optional.ofNullable(server).orElse(""));
        String po = HtmlUtils.htmlEscape(Optional.ofNullable(port).orElse(""));

        logger.info("MSTR Action URL : {}", actionUrl);
        logger.info("evt=" + evt + ", src=" + src + ", server=" + server + ", project=" + project + ", port=" + port);

        // Some MSTR versions expect fields like username, password, project, server, port, useSSL etc.
        String html = "<!doctype html>\n"
                + "<html><head><meta charset='utf-8'><title>Redirecting...</title> " +
                "<style>body {display:flex;align-items:center;justify-content:center;height:100vh;margin:0;background:#ffffff;font-family:sans-serif;color:#333;}" +
                ".loader {width:48px;height:48px;border:5px solid #ccc;border-top-color:#0078ff;border-radius:50%;animation:spin 0.9s linear infinite;margin:auto;}" +
                "@keyframes spin {to {transform:rotate(360deg);}}" +
                ".msg {margin-top:16px;text-align:center;font-size:15px;}" +
                "</style></head>\n"
                + "<body>\n"
                + "<form id='mstrForm' method='post' action='" + actionUrl + "'>\n"
                + "  <input type='hidden' name='Uid' value='" + escUser + "' />\n"
                + "  <input type='hidden' name='Pwd' value='" + escPwd + "' />\n"
                + "  <input name='ConnMode' id='ConnMode' type='hidden' class='mstrHiddenInput' value='1'>\n"
                + "  <input type='hidden' name='Project' value='" + pr + "'/>"
                + "  <input type='hidden' name='Server' value='" + sv + "'/>"
                + "  <input type='hidden' name='Port' value='" + po + "'/>"
                + "  <input name='evt' type='hidden' class='mstrHiddenInput' value='" + evt + "'>\n"
                + "  <input name='src' type='hidden' class='mstrHiddenInput' value='" + src + "'>\n"
                + "</form>\n"
                + "<script>document.getElementById('mstrForm').submit();</script>\n"
                + "<noscript><p>JavaScript required. <button onclick=\"document.getElementById('mstrForm').submit()\">Continue</button></p></noscript>\n"
                + "</body></html>";

        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
    }


    /**
     * Redirects the browser to the mstr error page with the provided fields.
     * All values are URL encoded and errorMessage is truncated to avoid giant URLs.
     */
    public void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                    String errorCode,
                                    String errorMessage,
                                    String project) throws IOException {


        String safeCode = errorCode == null ? "" : errorCode.trim();
        String safeMsg  = errorMessage == null ? "Unknown error" : errorMessage.trim();
        String safeProj = project == null ? "" : project.trim();

        // Build a CONTEXT-RELATIVE path: <contextPath>/error/mstr-error
        String targetPath = request.getContextPath() + "/error/mstr-error";

        String serverProp  = environment.getProperty(ApplicationConstants.SERVER);
        String serverValue = (serverProp == null || serverProp.isBlank()) ? "" : ApplicationConstants.HTTPS + serverProp;

        String redirect = UriComponentsBuilder
                .fromPath(targetPath)
                .queryParam("ErrorCode", safeCode)
                .queryParam("ErrorMessage", safeMsg)
                .queryParam("Project", safeProj)
                .queryParam("OriginalURL", request.getRequestURL().toString())
                .queryParam("Server", serverValue)
                .queryParam("Port", String.valueOf(request.getServerPort()))
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirect);
    }
}
