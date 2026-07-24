package com.symphony.applaunch.util;

import com.google.gson.JsonParser;
import com.symphony.applaunch.constants.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import javax.net.ssl.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.Objects;

@Component("qlikViewManagement")
@RequiredArgsConstructor
public class QlikViewManagement {

    private static final Logger logger = LoggerFactory.getLogger(QlikViewManagement.class);

    private final Environment environment;


    /**
     * This method is used to return Web ticket for qlickview
     *
     * @param username, document, domain
     * @return qlickview ticket as string
     */
    public String getQlikViewWebTicket(String username, String document, String domain) {
        try {
            // 1. Build XML (same as before)
            String groups = environment.getProperty(domain + "_groups");
            StringBuilder ugroups = new StringBuilder();
            if (!(groups == null || groups.isEmpty())) {
                ugroups.append("<GroupList>");
                String[] group = StringUtils.split(groups, ",");
                for (String grp : group) {
                    ugroups.append("<string>").append(grp).append("</string>");
                }
                ugroups.append("</GroupList>");
                ugroups.append("<GroupsIsNames>true</GroupsIsNames>");
            }

            String xmlString = String.format(
                    "<Global method=\"GetWebTicket\"><UserId>Client\\%s</UserId>%s</Global>",
                    username, ugroups
            );

            // 2. Read config
            String qvAdministrator = environment.getProperty(domain + "_QVAdministrator");
            String qvAdministratorPassword = environment.getProperty(domain + "_QVAdministratorPassword", "");
            String qvDomain = environment.getProperty(domain + "_domain");
            String qvUrl = environment.getProperty(domain + "_qlikViewServerURL", "");
            String hostName = Objects.requireNonNull(environment.getProperty(domain + "_url"));
            String scheme = environment.getProperty(domain + "_protocol", "https");

            // 3. NTLM credentials (HttpClient 4.x)
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    AuthScope.ANY,
                    new NTCredentials(qvAdministrator, qvAdministratorPassword, domain, qvDomain)
            );

            HttpHost targetHost = new HttpHost(hostName, 443, scheme);

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build()) {

                // 4. Build POST
                HttpPost post = new HttpPost(qvUrl);
                post.setHeader("Content-Type", "application/xml");

                StringEntity entity = new StringEntity(xmlString, ContentType.APPLICATION_XML);
                post.setEntity(entity);

                logger.info("Qlik view URL: {}", qvUrl);
                logger.info("Request XML: {}", xmlString);

                // 5. Execute
                try (CloseableHttpResponse response = httpClient.execute(targetHost, post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String body = EntityUtils.toString(response.getEntity(), java.nio.charset.StandardCharsets.UTF_8);

                    logger.info("QV status = {}, body = {}", statusCode, body);

                    if (statusCode == 200 && body != null) {
                        // 6. Parse token
                        InputSource source = new InputSource(new StringReader(body));
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        Object result = xpath.evaluate("/Global", source, XPathConstants.NODE);
                        String token = xpath.evaluate("_retval_", result);

                        return environment.getProperty(domain + "_domainName")
                                + "/qvajaxzfc/authenticate.aspx?type=html&try=/qvajaxzfc/opendoc.htm?document=" + document
                                + "&back=/LoginPage.htm&webticket=" + token;
                    } else {
                        logger.error("QV returned non-200 status: {} body: {}", statusCode, body);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE + e);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public String getQlikSenseWebTicket(String username, String certificatePath, String vproxy, String domain,
                                        String userdirectory, String userid) {
        try {

            if (domain != null && domain.isEmpty() && null == environment.getProperty(domain + "_xrfkey"))
                domain = "";
            else
                domain = domain + "_";

            String xrfkey = environment.getProperty(domain + "xrfkey");
            final String host = environment.getProperty(domain + "host");
            String ticket = "";
            logger.info("Qlik Sense Ticket....host is {}", host);
            logger.info("Qlik Sense Ticket....vproxy is {}", vproxy);
            logger.info("Qlik Sense Ticket....BEGIN Certificate Acquisition");

            String rootFile = environment.getProperty(domain + "rootCertificateFile");
            String clientFile = environment.getProperty(domain + "clientCertificateFile");

            logger.info("Configured rootCertificateFile: {}", rootFile);
            logger.info("Configured clientCertificateFile: {}", clientFile);

            String rootPath   = "qlikClientCertificate/" + rootFile + ".jks";
            String clientPath = "qlikClientCertificate/" + clientFile + ".jks";

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL rootUrl   = cl.getResource(rootPath);
            URL clientUrl = cl.getResource(clientPath);

            logger.info("Qlik Sense Ticket....rootUrl path = {}", rootPath);
            logger.info("Qlik Sense Ticket....rootUrl = {}", rootUrl);
            logger.info("Qlik Sense Ticket....clientUrl path = {}", clientPath);
            logger.info("Qlik Sense Ticket....clientUrl = {}", clientUrl);

            if (rootUrl == null || clientUrl == null) {
                logger.error("Qlik Sense Ticket....certificate not found on classpath. rootUrl={}, clientUrl={}",
                        rootUrl, clientUrl);
                return null;
            }

            // BEGIN Certificate Acquisition
            String proxyCertPass = environment.getProperty(domain + "proxyCertPass", "");
            String rootCertPass  = environment.getProperty(domain + "rootCertPass", "");
            // END Certificate Acquisition

            logger.info("Qlik Sense Ticket....BEGIN Certificate configuration for use in connection");

            // *** CHANGED PART: use InputStreams from classpath instead of File ***
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ksTrust = KeyStore.getInstance("JKS");

            try (InputStream clientStream = cl.getResourceAsStream(clientPath);
                 InputStream rootStream   = cl.getResourceAsStream(rootPath)) {

                if (clientStream == null) {
                    logger.error("Client keystore not found at {}", clientPath);
                    return null;
                }
                if (rootStream == null) {
                    logger.error("Root keystore not found at {}", rootPath);
                    return null;
                }

                // load client keystore
                ks.load(clientStream, proxyCertPass.toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, proxyCertPass.toCharArray());

                // load truststore
                ksTrust.load(rootStream, rootCertPass.toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ksTrust);

                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                SSLSocketFactory sslSocketFactory = context.getSocketFactory();
                // *** END CHANGED PART ***

                logger.info("Qlik Sense Ticket....BEGIN HTTPS Connection");
                if (vproxy != null && vproxy.equalsIgnoreCase("hub"))
                    vproxy = "ticket";

                URL url = new URL("https://" + host + ":4243/qps/" + vproxy + "/ticket?xrfkey=" + xrfkey);
                javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals(host));

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(sslSocketFactory);
                connection.setRequestProperty("x-qlik-xrfkey", xrfkey);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod("POST");

                logger.info("User DIrectory : {}", userdirectory);

                userdirectory = userdirectory.isEmpty() ? environment.getProperty(domain + "userDirectory")
                        : userdirectory;

                logger.info("User DIrectory : {}", userdirectory);

                logger.info("Qlik Sense Ticket....BEGIN JSON Message to Qlik Sense Proxy API");
                String body = "{ 'UserId':'" + username + "','UserDirectory':'" + userdirectory + "'," +
                        "'Attributes': []" +
                        "}";
                logger.info("Payload: {}", body);

                logger.info("Qlik Sense Ticket....Read Stream");
                try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
                    wr.write(body);
                    wr.flush();
                }

                StringBuilder builder = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        builder.append(inputLine);
                    }
                }

                String data = builder.toString();
                ticket = ((new JsonParser().parse(data)).getAsJsonObject())
                        .get("Ticket").toString().replace("\"", "");

                return ticket;
            }

        } catch (Exception e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE, e);
        }
        logger.info("Qlik Sense Ticket....Error return null");
        return null;
    }
}
