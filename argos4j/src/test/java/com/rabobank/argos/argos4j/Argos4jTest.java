package com.rabobank.argos.argos4j;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Argos4jTest {

    private Argos4j argos4j;
    private WireMockServer wireMockServer;

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() throws IOException {
        Integer randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();
        setupStub();


        Argos4jSettings settings = Argos4jSettings.builder()
                .baseUrl("http://localhost:" + randomPort + "/api")
                .stepName("build")
                .supplyChainId("supplyChainId")
                .signingKey(SigningKey.builder()
                        .key(IOUtils.toByteArray(getClass().getResourceAsStream("/my-passless-private.key")))
                        .build()).build();
        argos4j = new Argos4j(settings);


    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void setupStub() {
        wireMockServer.stubFor(post(urlEqualTo("/api/link/supplyChainId")).willReturn(noContent()));
    }

    @Test
    void storeMetablockLinkForDirectory() {
        argos4j.storeMetablockLinkForDirectory(new File("."), new File("."));
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertEquals(1, requests.size());
    }
}