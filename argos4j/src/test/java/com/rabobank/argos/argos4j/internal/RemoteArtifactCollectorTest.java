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
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.FileCollectorSettings;
import com.rabobank.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@Slf4j
class RemoteArtifactCollectorTest {

    private ArtifactCollector collector;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() throws IOException {

        Integer randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();

        collector = ArtifactCollectorFactory.build(FileCollector.builder()
                .type(FileCollector.FileCollectorType.REMOTE_ZIP)
                .settings(FileCollectorSettings.builder().build())
                .uri(URI.create("http://bart:secret@localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar")).build());
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    void collect() throws IOException {
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

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}