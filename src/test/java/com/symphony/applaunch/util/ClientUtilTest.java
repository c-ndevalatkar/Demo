package com.symphony.applaunch.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientUtilTest {

    private final ClientUtil clientUtil = new ClientUtil();

    private HttpServletRequest mockRequestWithUserAgent(String userAgent) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        return request;
    }

    @Test
    void getClientBrowser_shouldDetectIE_withMsieToken() {
        String ua = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("IE-10.0", browser);
    }

    @Test
    void getClientBrowser_shouldDetectOpera_legacyOperaToken() {
        String ua = "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        // Opera + Version branch -> Opera-12.14
        assertEquals("Opera-12.14", browser);
    }

    @Test
    void getClientBrowser_shouldDetectOpera_oprToken() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36 OPR/89.0.4447.83";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        // OPR -> Opera-<version>
        assertEquals("Opera-89.0.4447.83", browser);
    }

    @Test
    void getClientBrowser_shouldDetectNetscape_likeOldMozilla() {
        String ua = "Mozilla/7.0 (Windows NT 5.1)";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("Netscape-?", browser);
    }

    @Test
    void getClientBrowser_shouldDetectEdge_withEdgToken() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("Edg-110.0.1587.57", browser);
    }

    @Test
    void getClientBrowser_shouldDetectChrome() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/120.0.0.0 Safari/537.36";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("Chrome-120.0.0.0", browser);
    }

    @Test
    void getClientBrowser_shouldDetectSafari_withVersion() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                + "AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "Version/16.0 Safari/605.1.15";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        // Safari + Version -> Safari-16.0
        assertEquals("Safari-16.0", browser);
    }

    @Test
    void getClientBrowser_shouldDetectFirefox() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:115.0) "
                + "Gecko/20100101 Firefox/115.0";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("Firefox-115.0", browser);
    }

    @Test
    void getClientBrowser_shouldDetectIEviaRvToken() {
        // IE 11 style UA: contains rv but not the old MSIE token
        String ua = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals("IE", browser);
    }

    @Test
    void getClientBrowser_shouldFallbackToRawHeader_whenNoKnownBrowserMatches() {
        String ua = "CustomBrowser/1.0 (Some OS)";
        HttpServletRequest request = mockRequestWithUserAgent(ua);

        String browser = clientUtil.getClientBrowser(request);

        assertEquals(ua, browser);
    }
}
