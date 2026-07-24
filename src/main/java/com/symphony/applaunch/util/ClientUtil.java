package com.symphony.applaunch.util;

import org.springframework.stereotype.Component;

@Component("clientUtil")
public class ClientUtil {

    /**
     * This method is used to get Client browser details
     *
     * @return browser
     */
    public String getClientBrowser(jakarta.servlet.http.HttpServletRequest request) {
        final String browserDetails = request.getHeader("User-Agent");
        final String user = browserDetails.toLowerCase();

        String browser = "";

        // ===============Browser===========================
        if (user.contains("msie")) {
            String substring = browserDetails.substring(browserDetails.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        } else if (user.contains("opr") || user.contains("opera")) {
            if (user.contains("opera"))
                browser = (browserDetails.substring(browserDetails.indexOf("Opera")).split(" ")[0]).split("/")[0] + "-"
                        + (browserDetails.substring(browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
            else if (user.contains("opr"))
                browser = ((browserDetails.substring(browserDetails.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                        .replace("OPR", "Opera");
        } else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1)
                || (user.indexOf("mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1)
                || (user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1)) {

            browser = "Netscape-?";

        } else if (user.contains("edg")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Edg")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("chrome")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("safari") && user.contains("version")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Safari")).split(" ")[0]).split("/")[0] + "-"
                    + (browserDetails.substring(browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
        } else if (user.contains("firefox")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("rv")) {
            browser = "IE";
        } else {
            browser = browserDetails;
        }

        return browser;
    }

}
