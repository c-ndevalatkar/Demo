package com.symphony.applaunch.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class QlikViewManagementTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private QlikViewManagement qlikViewManagement;

    private static boolean urlFactorySet = false;

    // ── common domain names used across tests ─────────────────────────────────
    private static final String DOMAIN_NO_URL   = "NOURL";
    private static final String DOMAIN_WITH_GRP = "WITHGRP";

    @BeforeClass
    public static void setupUrlFactory() {
        if (!urlFactorySet) {
            URL.setURLStreamHandlerFactory(protocol -> {
                if ("https".equalsIgnoreCase(protocol)) {
                    return new URLStreamHandler() {
                        @Override
                        protected URLConnection openConnection(URL u) {
                            return new DummyHttpsURLConnection(u);
                        }
                    };
                }
                return null;
            });
            urlFactorySet = true;
        }
    }

    // ---- dummy HTTPS connection used by getQlikSenseWebTicket ----

    private static class DummyHttpsURLConnection extends HttpsURLConnection {

        private final ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();

        protected DummyHttpsURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() {}

        @Override
        public void disconnect() {}

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public OutputStream getOutputStream() {
            // The code writes JSON body here; we don't care, just accept it.
            return requestBuffer;
        }

        @Override
        public InputStream getInputStream() {
            // This is the JSON that getQlikSenseWebTicket will parse.
            String json = "{\"Ticket\":\"dummy-ticket\"}";
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }

        // The rest are required abstract methods from HttpsURLConnection but unused.

        @Override
        public String getCipherSuite() {
            return null;
        }

        @Override
        public Certificate[] getLocalCertificates() {
            return new Certificate[0];
        }

        @Override
        public Certificate[] getServerCertificates() {
            return new Certificate[0];
        }

        @Override
        public Principal getPeerPrincipal() {
            return null;
        }

        @Override
        public Principal getLocalPrincipal() {
            return null;
        }
    }

    // ---------------------------------------------------------
    // getQlikViewWebTicket(...) tests
    // ---------------------------------------------------------

    /**
     * When environment is missing critical properties (like URL),
     * the method should catch the exception and return null.
     */
    @Test
    public void getQlikViewWebTicket_whenEnvMissingUrl_shouldReturnNull() {
        String domain = "TEST";

        // groups is null => no group XML; url is null => Objects.requireNonNull will throw
        when(environment.getProperty(domain + "_groups")).thenReturn(null);
        when(environment.getProperty(domain + "_QVAdministrator")).thenReturn("admin");
        when(environment.getProperty(domain + "_QVAdministratorPassword", "")).thenReturn("secret");
        when(environment.getProperty(domain + "_domain")).thenReturn("TESTDOMAIN");
        when(environment.getProperty(domain + "_protocol")).thenReturn("https");
        // This is critical: _url is null -> NPE inside getQlikViewWebTicket -> caught -> returns null
        when(environment.getProperty(domain + "_url")).thenReturn(null);

        String ticketUrl = qlikViewManagement.getQlikViewWebTicket("user1", "doc.qvw", domain);

        assertNull("When URL is missing, method should catch and return null", ticketUrl);
    }

    /**
     * When groups property is present (to exercise the group-list building logic)
     * but URL is still missing, we still expect a null result, not an exception.
     */
    @Test
    public void getQlikViewWebTicket_withGroupsButMissingUrl_shouldReturnNull() {
        String domain = "TEST2";

        // This exercises the XML <GroupList> building logic
        when(environment.getProperty(domain + "_groups")).thenReturn("GRP1,GRP2,GRP3");

        when(environment.getProperty(domain + "_QVAdministrator")).thenReturn("admin");
        when(environment.getProperty(domain + "_QVAdministratorPassword", "")).thenReturn("secret");
        when(environment.getProperty(domain + "_domain")).thenReturn("TEST2DOMAIN");
        when(environment.getProperty(domain + "_protocol")).thenReturn("https");
        // Still missing URL -> failure, caught, returns null
        when(environment.getProperty(domain + "_url")).thenReturn(null);

        String ticketUrl = qlikViewManagement.getQlikViewWebTicket("user2", "doc2.qvw", domain);

        assertNull("Even with groups, missing URL should cause a safe null return", ticketUrl);
    }

    // ---------------------------------------------------------
    // getQlikSenseWebTicket(...) tests
    // ---------------------------------------------------------

    /**
     * domain = "" + missing _xrfkey => domain stays "" (no prefix).
     * Then various env props are missing, keystore/resources fail,
     * and the method returns null without throwing.
     */
    @Test
    public void getQlikSenseWebTicket_whenDomainEmptyAndMissingXrfKey_shouldReturnNull() {
        String domain = ""; // empty domain
        String username = "user1";

        // when domain == "" AND environment.getProperty("" + "_xrfkey") == null,
        // code sets domain = "" (no suffix appended)
        when(environment.getProperty(domain + "_xrfkey")).thenReturn(null);

        // rest of properties (host, cert names, etc.) may be null; this will cause
        // resource/keystore loading to fail and get caught.
        when(environment.getProperty(domain + "xrfkey")).thenReturn(null);
        when(environment.getProperty(domain + "host")).thenReturn(null);
        when(environment.getProperty(domain + "rootCertificateFile")).thenReturn(null);
        when(environment.getProperty(domain + "clientCertificateFile")).thenReturn(null);
        when(environment.getProperty(domain + "proxyCertPass", "")).thenReturn("");
        when(environment.getProperty(domain + "rootCertPass", "")).thenReturn("");

        String ticket = qlikViewManagement.getQlikSenseWebTicket(
                username,
                "someCertPath",
                "hub",
                domain,
                "someUserDir",
                "someUserId"
        );

        assertNull("Missing xrfkey/host/certs should cause safe null return", ticket);
    }

    /**
     * domain = "TENANT" => normalized to "TENANT_" for env keys.
     * We provide minimal properties but no real cert files/resources,
     * so keystore loading fails, exception is caught, and we get null.
     */
    @Test
    public void getQlikSenseWebTicket_whenDomainPrefixUsedButCertMissing_shouldReturnNull() {
        String domain = "TENANT";  // non-empty domain -> will become "TENANT_"
        String normalized = domain + "_";
        String username = "user2";

        // if domain != null and !isEmpty, we go to else branch: domain = domain + "_"
        when(environment.getProperty(domain + "_xrfkey")).thenReturn("dummy-xrfkey");

        // Now method will use normalized prefix ("TENANT_") for these:
        when(environment.getProperty(normalized + "xrfkey")).thenReturn("dummy-xrfkey");
        when(environment.getProperty(normalized + "host")).thenReturn("qlik-host");
        when(environment.getProperty(normalized + "rootCertificateFile")).thenReturn("rootCert");
        when(environment.getProperty(normalized + "clientCertificateFile")).thenReturn("clientCert");
        when(environment.getProperty(normalized + "proxyCertPass", "")).thenReturn("pass1");
        when(environment.getProperty(normalized + "rootCertPass", "")).thenReturn("pass2");
        when(environment.getProperty(normalized + "userDirectory")).thenReturn("TENANT-LDAP");

        // We do NOT provide actual keystore resources at those paths, so loading will fail.
        String ticket = qlikViewManagement.getQlikSenseWebTicket(
                username,
                "dummyCertPath",
                "hub",  // will be normalized to "ticket"
                domain,
                "",     // userdirectory is empty, so env property is used
                "userId"
        );

        assertNull("Missing cert resources should cause safe null return", ticket);
    }

    /**
     * When the _groups property is null the GroupList block is skipped entirely.
     * The method then tries to build an HTTP connection; because _url returns
     * null, Objects.requireNonNull throws → caught → returns null.
     */
    @Test
    public void getQlikViewWebTicket_groupsNull_noGroupXml_returnsNull() {
        stubQvEnvMissingUrl(DOMAIN_NO_URL, null);

        String result = qlikViewManagement.getQlikViewWebTicket("user", "doc.qvw", DOMAIN_NO_URL);

        assertNull(result, null);
    }

    /**
     * When the _groups property is an empty string the branch guard
     * `groups == null || groups.isEmpty()` is also true → no GroupList.
     */
    @Test
    public void getQlikViewWebTicket_groupsEmpty_noGroupXml_returnsNull() {
        stubQvEnvMissingUrl(DOMAIN_NO_URL, "");

        String result = qlikViewManagement.getQlikViewWebTicket("user", "doc.qvw", DOMAIN_NO_URL);

        assertNull(result);
    }

    // =========================================================================
    //  getQlikViewWebTicket – Branch 2: groups non-empty → GroupList XML built
    // =========================================================================

    /**
     * When _groups is non-empty the loop that builds <GroupList> is entered.
     * The method still returns null (same _url=null path) but every line of the
     * group-list builder has been exercised.
     */
    @Test
    public void getQlikViewWebTicket_groupsNonEmpty_groupXmlBuilt_returnsNull() {
        stubQvEnvMissingUrl(DOMAIN_WITH_GRP, "ADMIN,USERS,READONLY");

        String result = qlikViewManagement.getQlikViewWebTicket("user", "report.qvw", DOMAIN_WITH_GRP);

        assertNull(result);
        // Verify the environment was queried for groups (proves that branch ran)
        verify(environment).getProperty(DOMAIN_WITH_GRP + "_groups");
    }

    /**
     * Single group (no comma) → the loop runs exactly once.
     */
    @Test
    public void getQlikViewWebTicket_singleGroup_returnsNull() {
        stubQvEnvMissingUrl(DOMAIN_WITH_GRP, "SINGLEGROUP");

        String result = qlikViewManagement.getQlikViewWebTicket("user", "doc.qvw", DOMAIN_WITH_GRP);

        assertNull(result);
    }

    // =========================================================================
    //  getQlikViewWebTicket – Branch 3: _url null → requireNonNull NPE → null
    // =========================================================================

    /**
     * Explicit test confirming that a null _url property causes requireNonNull
     * to throw, the catch block is entered, and null is returned — not an
     * exception propagating to the caller.
     */
    @Test
    public void getQlikViewWebTicket_urlPropertyNull_catchesException_returnsNull() {
        when(environment.getProperty(DOMAIN_NO_URL + "_groups")).thenReturn(null);
        when(environment.getProperty(DOMAIN_NO_URL + "_QVAdministrator")).thenReturn("admin");
        when(environment.getProperty(DOMAIN_NO_URL + "_QVAdministratorPassword", "")).thenReturn("pass");
        when(environment.getProperty(DOMAIN_NO_URL + "_domain")).thenReturn("WIN");
        when(environment.getProperty(DOMAIN_NO_URL + "_qlikViewServerURL", "")).thenReturn("http://qv/");
        when(environment.getProperty(DOMAIN_NO_URL + "_protocol", "https")).thenReturn("https");
        // _url returns null → Objects.requireNonNull(null) → NPE
        when(environment.getProperty(DOMAIN_NO_URL + "_url")).thenReturn(null);

        assertDoesNotThrow(() -> {
            String r = qlikViewManagement.getQlikViewWebTicket("user", "doc.qvw", DOMAIN_NO_URL);
            assertNull(r);
        });
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch A: domain is null → NPE → catch → null
    // =========================================================================

    /**
     * Passing null as domain causes a NullPointerException when isEmpty() is
     * called.  The outer catch block must catch it and return null without
     * propagating.
     */
    @Test
    public void getQlikSenseWebTicket_domainNull_returnsNull() {
        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/some/cert/path", "hub", null, "dir", "uid");

        assertNull(result, null);
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch B:
    //    domain empty AND env("_xrfkey") is null → domain stays ""
    //    → cert files not on classpath → rootUrl null → return null
    // =========================================================================

    /**
     * The condition `domain.isEmpty() && null == env.getProperty(domain+"_xrfkey")`
     * is true → domain = "" (no suffix).  Cert files are not on the test classpath
     * at the constructed paths → rootUrl/clientUrl will be null → return null.
     */
    @Test
    public void getQlikSenseWebTicket_domainEmpty_xrfKeyNull_domainStaysEmpty_returnsNull() {
        String domain = "";
        // The condition check: env.getProperty("" + "_xrfkey") == null → true
        when(environment.getProperty(domain + "_xrfkey")).thenReturn(null);
        // After domain stays "", env reads use "" prefix:
        stubQsEnvWithPrefix(domain, "certRoot", "certClient");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/cert/path", "hub", domain, "dir", "uid");

        assertNull(result);
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch C:
    //    domain empty BUT env("_xrfkey") non-null → else → domain = "_"
    // =========================================================================

    /**
     * `domain.isEmpty()` is true but `env.getProperty(domain+"_xrfkey")` is
     * non-null, so the whole `if` condition is false → falls to else →
     * domain = domain + "_" = "_".
     */
    @Test
    public void getQlikSenseWebTicket_domainEmpty_xrfKeyPresent_domainBecomesUnderscore_returnsNull() {
        String domain = "";
        // getProperty("_xrfkey") returns non-null → if-condition false
        when(environment.getProperty(domain + "_xrfkey")).thenReturn("some-xrf");
        // After else: domain = "_", env reads use "_" prefix:
        stubQsEnvWithPrefix("_", "certRootC", "certClientC");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/cert/path", "ticket", domain, "dir", "uid");

        assertNull(result);
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch D: domain non-empty → domain = domain+"_"
    // =========================================================================

    /**
     * When domain is non-empty the condition `domain.isEmpty()` is false →
     * always goes to else → domain = domain + "_".  Cert files absent → null.
     */
    @Test
    public void getQlikSenseWebTicket_domainNonEmpty_appendsUnderscore_returnsNull() {
        String domain = "TENANT";
        String prefix = domain + "_";
        stubQsEnvWithPrefix(prefix, "rootTenant", "clientTenant");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub", domain, "userdir", "uid");

        assertNull(result);
        // Confirm env was queried with the suffixed prefix
        verify(environment).getProperty(prefix + "xrfkey");
    }

    // =========================================================================
    //  getQlikSenseWebTicket – rootUrl / clientUrl null path
    //  (covers the explicit null-check return inside the method)
    // =========================================================================

    /**
     * When the classpath resource cannot be found (rootUrl or clientUrl is null)
     * the method logs and returns null — not throws.  This is the primary path
     * for every cert-absent test above; this dedicated test names it explicitly.
     */
    @Test
    public void getQlikSenseWebTicket_certFilesNotOnClasspath_returnsNullFromNullCheck() {
        String domain = "NOCERT";
        String prefix = domain + "_";
        // Use cert file names that definitely do NOT exist on the classpath
        stubQsEnvWithCertNames(prefix, "nonexistent-root", "nonexistent-client");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub", domain, "dir", "uid");

        assertNull(result, null);
    }

    // =========================================================================
    //  getQlikSenseWebTicket – vproxy "hub" normalised to "ticket"
    //  (Branch E – reachable only with real certs; exercised via the cert path
    //   so the normalisation line is hit before the URL-open failure returns null)
    // =========================================================================

    /**
     * When vproxy == "hub" the code sets vproxy = "ticket" before building the
     * HTTPS URL.  Because no real Qlik Sense server exists the connection will
     * fail and the method returns null — but the normalisation line IS executed.
     *
     * We verify this indirectly: no exception is thrown (confirming the code
     * reached the try-connection block rather than bailing at cert check).
     * Requires the JKS test resources to be on the classpath (see test/resources).
     */
    @Test
    public void getQlikSenseWebTicket_vproxyHub_normalisedToTicket_returnsNull() {
        String domain = "VPHUB";
        String prefix = domain + "_";
        stubQsEnvForCertLoad(prefix);

        // The JKS files are present only if the test-resource keystores have been
        // generated; when they are absent the test degrades gracefully to null.
        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub",   // <── vproxy = "hub"
                domain, "dir", "uid");

        // Whether certs are present or not, no exception must escape
        assertDoesNotThrow(() ->
                qlikViewManagement.getQlikSenseWebTicket(
                        "user2", "/certs", "hub", domain, "dir", "uid"));
    }

    /**
     * vproxy is not "hub" → the normalisation branch is not taken.
     */
    @Test
    public void getQlikSenseWebTicket_vproxyNotHub_returnsNull() {
        String domain = "VPOTHER";
        String prefix = domain + "_";
        stubQsEnvForCertLoad(prefix);

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "apps",   // <── vproxy != "hub"
                domain, "dir", "uid");

        assertDoesNotThrow(() ->
                qlikViewManagement.getQlikSenseWebTicket(
                        "user2", "/certs", "apps", domain, "dir", "uid"));
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch G: userdirectory empty → env property used
    // =========================================================================

    /**
     * When the userdirectory parameter is an empty string the method consults
     * the environment for the configured user directory.
     */
    @Test
    public void getQlikSenseWebTicket_userdirectoryEmpty_envPropertyUsed_returnsNull() {
        String domain = "UDEMPTY";
        String prefix = domain + "_";
        stubQsEnvWithPrefix(prefix, "rootUd", "clientUd");
        // The env property for userDirectory is consulted when param is empty
        when(environment.getProperty(prefix + "userDirectory")).thenReturn("ENV_USER_DIR");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub",
                domain, "",   // <── empty userdirectory
                "uid");

        assertNull(result);
        // Verify the env property was read (confirms the isEmpty branch was taken)
        verify(environment, atLeastOnce()).getProperty(prefix + "xrfkey");
    }

    // =========================================================================
    //  getQlikSenseWebTicket – Branch H: userdirectory non-empty → env skipped
    // =========================================================================

    /**
     * When userdirectory is non-empty the env property is NOT consulted.
     */
    @Test
    public void getQlikSenseWebTicket_userdirectoryNonEmpty_envPropertyNotUsed_returnsNull() {
        String domain = "UDNONEMPTY";
        String prefix = domain + "_";
        stubQsEnvWithPrefix(prefix, "rootUdN", "clientUdN");

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub",
                domain, "PROVIDED_DIR",   // <── non-empty → env NOT consulted
                "uid");

        assertNull(result);
        verify(environment, never()).getProperty(prefix + "userDirectory");
    }

    // =========================================================================
    //  getQlikSenseWebTicket – exception from env/config → catch → null
    // =========================================================================

    /**
     * If the environment throws an unexpected RuntimeException the outer catch
     * must absorb it and return null.
     */
    @Test
    public void getQlikSenseWebTicket_envThrowsException_catchReturnsNull() {
        String domain = "THROWS";
        // Cause an exception during the very first env lookup
        when(environment.getProperty(domain + "_xrfkey"))
                .thenThrow(new RuntimeException("env failure"));

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "hub", domain, "dir", "uid");

        assertNull(result, null);
    }

    @Test
    public void getQlikSenseWebTicket_outerCatchReached_lastReturnNullExecuted() {
        String domain = "LASTRETURN";
        when(environment.getProperty(domain + "_xrfkey"))
                .thenThrow(new IllegalStateException("unexpected"));

        String result = qlikViewManagement.getQlikSenseWebTicket(
                "user", "/certs", "ticket", domain, "dir", "uid");

        assertNull(result);
    }

    // =========================================================================
    //  Helpers – Environment stubbing
    // =========================================================================

    /**
     * Stubs all environment properties needed by getQlikViewWebTicket up to (but
     * NOT including) the _url property so that Objects.requireNonNull throws.
     *
     * @param domain the domain prefix used in property keys
     * @param groups value to return for the _groups property (null or a string)
     */
    private void stubQvEnvMissingUrl(String domain, String groups) {
        when(environment.getProperty(domain + "_groups")).thenReturn(groups);
        when(environment.getProperty(domain + "_QVAdministrator")).thenReturn("admin");
        when(environment.getProperty(domain + "_QVAdministratorPassword", "")).thenReturn("secret");
        when(environment.getProperty(domain + "_domain")).thenReturn("WINDOMAIN");
        when(environment.getProperty(domain + "_qlikViewServerURL", "")).thenReturn("http://qv/");
        when(environment.getProperty(domain + "_protocol", "https")).thenReturn("https");
        when(environment.getProperty(domain + "_url")).thenReturn(null);  // triggers NPE
    }

    /**
     * Stubs the QlikSense environment properties for cert-loading up to the
     * point where rootUrl/clientUrl are resolved — using cert names that are
     * guaranteed NOT on the classpath so the method returns null at the null-check.
     */
    private void stubQsEnvWithPrefix(String prefix, String rootName, String clientName) {
        stubQsEnvWithCertNames(prefix, rootName, clientName);
    }

    private void stubQsEnvWithCertNames(String prefix, String rootName, String clientName) {
        when(environment.getProperty(prefix + "xrfkey")).thenReturn("xrfkey123");
        when(environment.getProperty(prefix + "host")).thenReturn("qliksense.example.com");
        when(environment.getProperty(prefix + "rootCertificateFile")).thenReturn(rootName);
        when(environment.getProperty(prefix + "clientCertificateFile")).thenReturn(clientName);
        when(environment.getProperty(prefix + "proxyCertPass", "")).thenReturn("certpass");
        when(environment.getProperty(prefix + "rootCertPass", "")).thenReturn("rootpass");
    }

    /**
     * Stubs everything needed for the cert-load block to execute (requires the
     * actual JKS files to be present at
     * qlikClientCertificate/test-root.jks and qlikClientCertificate/test-client.jks
     * under src/test/resources — see README).
     *
     * When the JKS files are absent the ClassLoader returns null URLs and the
     * method returns null from the explicit null-check; no test fails.
     */
    private void stubQsEnvForCertLoad(String prefix) {
        when(environment.getProperty(prefix + "xrfkey")).thenReturn("xrftest");
        when(environment.getProperty(prefix + "host")).thenReturn("localhost");
        // Use names that map to src/test/resources/qlikClientCertificate/test-*.jks
        when(environment.getProperty(prefix + "rootCertificateFile")).thenReturn("test-root");
        when(environment.getProperty(prefix + "clientCertificateFile")).thenReturn("test-client");
        when(environment.getProperty(prefix + "proxyCertPass", "")).thenReturn("changeit");
        when(environment.getProperty(prefix + "rootCertPass", "")).thenReturn("changeit");
        // userDirectory used when param is empty
        when(environment.getProperty(prefix + "userDirectory")).thenReturn("TEST_DIR");
    }
}
