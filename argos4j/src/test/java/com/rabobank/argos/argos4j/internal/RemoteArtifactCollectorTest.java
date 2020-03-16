/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.argos4j.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.RemoteFileCollector;
import com.rabobank.argos.argos4j.RemoteZipFileCollector;
import com.rabobank.argos.domain.link.Artifact;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteArtifactCollectorTest {

    private ArtifactCollector collector;
    private WireMockServer wireMockServer;
    private Integer randomPort;

    @BeforeEach
    void setUp() throws IOException {
        randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    void collectRemoteZip() throws IOException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();
        assertThat(collect, contains(
                Artifact.builder().uri("META-INF/MANIFEST.MF").hash("53e5e0a85a6aefa827e2fe34748cd1030c02a492bd9b309dc2f123258a218901").build(),
                Artifact.builder().uri("argos-test-app.war/argos-test-app.war").hash("f5e94511d66ffbd76e164b7a5c8ec91727f6435dabce365b53e7f4221edd88ae").build(),
                Artifact.builder().uri("deployit-manifest.xml").hash("9c1a8531bbd86414d6cc9929daa19d06a05cf3ca335b4ca7abe717c8f2b5f3ec").build()));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), is("Basic YmFydDpzZWNyZXQ="));
    }

    @Test
    void collectEncryptedRemoteZip() throws IOException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/with-password.zip")))));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("encrypted ZIP entry not supported"));
    }

    @Test
    void collectNotFound() throws MalformedURLException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(notFound()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("call to http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar returned 404"));
    }


    @Test
    void collectNotAuthorized() throws MalformedURLException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(status(401).withBody("Not authorized")));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("call to http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar returned 401 with body : Not authorized"));
    }

    @Test
    void collectConnectionRefused() throws MalformedURLException {
        randomPort = 33321;
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(status(401).withBody("Not authorized")));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("http://localhost:33321/argos-test-app-1.0-SNAPSHOT.dar got error Connection refused (Connection refused)"));
    }

    private void createZipCollector() throws MalformedURLException {
        collector = ArtifactCollectorFactory.build(RemoteZipFileCollector.builder().username("bart")
                .password("secret".toCharArray()).url(new URL("http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar")).build());
    }

    private void createFileCollector(String artifactUri) throws MalformedURLException {
        collector = ArtifactCollectorFactory.build(RemoteFileCollector.builder()
                .artifactUri(artifactUri).url(new URL("http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar")).build());
    }

    @Test
    void collectRemoteFileWithConfiguredArtifactName() throws IOException {

        createFileCollector("other.war");

        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();
        assertThat(collect, contains(
                Artifact.builder().uri("other.war").hash("95540f95db610e211bed84c09f1badb42560806d940e7f4d8209c4f2d3880b7d").build()));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), nullValue());
    }

    @Test
    void collectRemoteFile() throws IOException {

        createFileCollector(null);

        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();
        assertThat(collect, contains(
                Artifact.builder().uri("argos-test-app-1.0-SNAPSHOT.dar").hash("95540f95db610e211bed84c09f1badb42560806d940e7f4d8209c4f2d3880b7d").build()));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), nullValue());
    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}