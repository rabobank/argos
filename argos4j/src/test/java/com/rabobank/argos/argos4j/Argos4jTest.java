package com.rabobank.argos.argos4j;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Argos4jTest {

    private Argos4j argos4j;
    private WireMockServer wireMockServer;

    @TempDir
    static File sharedTempDir;

    @BeforeAll
    static void setUpBefore() throws IOException {
        FileUtils.write(new File(sharedTempDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchAlgorithmException {
        Integer randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("http://localhost:" + randomPort + "/api")
                .stepName("build")
                .supplyChainName("supplyChainName")
                .signingKey(SigningKey.builder().keyPair(pair).build())
                .build();
        argos4j = new Argos4j(settings);

    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void storeMetablockLinkForDirectory() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(ok().withBody("[{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\"}]")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link/")).willReturn(noContent()));
        argos4j.collectMaterials(sharedTempDir.getAbsoluteFile());
        argos4j.collectProducts(sharedTempDir.getAbsoluteFile());
        argos4j.store();
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(2));
        assertThat(requests.get(1).getBodyAsString(), endsWith(",\"link\":{\"materials\":[{\"uri\":\"text.txt\",\"hash\":\"616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b\"}],\"stepName\":\"build\",\"products\":[{\"uri\":\"text.txt\",\"hash\":\"616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b\"}]}}"));
    }

    @Test
    void storeMetablockLinkForDirectoryFailed() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(ok().withBody("[{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\"}]")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link/")).willReturn(serverError()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> argos4j.store());
        assertThat(error.getMessage(), is("500 "));
    }

    @Test
    void storeMetablockLinkForDirectoryUnexectedResonse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(badRequest()));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link/")).willReturn(ok()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> argos4j.store());
        assertThat(error.getMessage(), is("400 "));
    }
}