package com.rabobank.argos.test;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.KeyIdProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.stream.Stream;

import static com.rabobank.argos.test.TestHelper.clearDatabase;
import static com.rabobank.argos.test.TestHelper.getKeyApiApi;
import static com.rabobank.argos.test.TestHelper.getSupplychainApi;
import static com.rabobank.argos.test.TestHelper.waitForArgosServiceToStart;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class JenkinsTestIT {

    private static Properties properties = Properties.getInstance();
    private static final String SERVER_BASEURL = "server.baseurl";
    @BeforeAll
    static void startup() {
        log.info("jenkins base url : {}", properties.getJenkinsBaseUrl());
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        waitForJenkinsToStart();
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void setUp() {
        clearDatabase();
    }

    private static void waitForJenkinsToStart() {
        log.info("Waiting for jenkins start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(1, MINUTES).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getJenkinsBaseUrl() + "/login"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });
        log.info("jenkins started");
    }

    @Test
    public void testFreestyle() throws IOException, URISyntaxException {
        getSupplychainApi().createSupplyChain(new RestCreateSupplyChainCommand().name("argos-test-app"));
        PublicKey publicKey = getPemKeyPair(getClass().getResourceAsStream("/bob")).getPublic();
        getKeyApiApi().storeKey(new RestKeyPair().publicKey(publicKey.getEncoded()).keyId(new KeyIdProviderImpl().computeKeyId(publicKey)));
        JenkinsServer jenkins = new JenkinsServer(new URI(properties.getJenkinsBaseUrl()), "admin", "admin");
        await().atMost(10, SECONDS).until(() -> getJob(jenkins) != null);
        JobWithDetails job = getJob(jenkins);
        QueueReference reference = job.build();

        await().atMost(25, SECONDS).until(() -> jenkins.getQueueItem(reference).getExecutable() != null);

        QueueItem queueItem = jenkins.getQueueItem(reference);
        Build build = jenkins.getBuild(queueItem);

        int buildNumber = build.getNumber();

        log.info("build number {}", buildNumber);

        await().atMost(2, MINUTES).until(() -> !build.details().isBuilding());

        Build lastSuccessfulBuild = getJob(jenkins).getLastSuccessfulBuild();
        Build lastUnsuccessfulBuild = getJob(jenkins).getLastUnsuccessfulBuild();

        if(lastUnsuccessfulBuild != Build.BUILD_HAS_NEVER_RUN) {
            Stream.of(lastUnsuccessfulBuild.details().getConsoleOutputText().split("\\r?\\n")).forEach(log::error);
        }

        assertThat(lastUnsuccessfulBuild.getNumber(), is(-1));
        assertThat(lastSuccessfulBuild.getNumber(), is(buildNumber));
    }

    private JobWithDetails getJob(JenkinsServer jenkins) throws IOException {
        return jenkins.getJob("argos-test-app-freestyle-recording");
    }

    private static KeyPair getPemKeyPair(InputStream signingKey) {
        try (Reader reader = new InputStreamReader(signingKey, StandardCharsets.UTF_8);
             PEMParser pemReader = new PEMParser(reader)) {
            Object pem = pemReader.readObject();
            PEMKeyPair kpr;
            if (pem instanceof PEMKeyPair) {
                kpr = (PEMKeyPair) pem;
            } else if (pem instanceof SubjectPublicKeyInfo) {
                kpr = new PEMKeyPair((SubjectPublicKeyInfo) pem, null);
            } else {
                throw new Argos4jError("Couldn't parse PEM object: " + pem.toString());
            }
            return new JcaPEMKeyConverter().getKeyPair(kpr);
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }

}
