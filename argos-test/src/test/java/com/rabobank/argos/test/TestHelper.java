/*
 * Copyright (C) 2019 Rabobank Nederland
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
package com.rabobank.argos.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.KeyApi;
import com.rabobank.argos.argos4j.rest.api.client.LayoutApi;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class TestHelper {

    private static Properties properties = Properties.getInstance();

    public static void clearDatabase() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getIntegrationTestServiceBaseUrl() + "/integration-test/reset-db"))
                    .method("POST", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(send.statusCode(), is(200));
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public static void waitForArgosServiceToStart() {
        log.info("Waiting for argos service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(30, SECONDS).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getApiBaseUrl() + "/actuator/health"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("argos service started");
    }
    
    public static RestLayoutMetaBlock signLayout(String layout) throws JsonMappingException, JsonProcessingException {
        String response = "";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getIntegrationTestServiceBaseUrl() + "/integration-test/signLayoutMetaBlock"))
                    .method("POST", HttpRequest.BodyPublishers.ofString(layout))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(send.statusCode(), is(200));
            response = send.body();
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        
        return new ObjectMapper().readValue(response, RestLayoutMetaBlock.class);
    }
    
    public static String getSnapshotHash() {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getNexusSnapshotUrl()))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assertThat(send.statusCode(), is(200));
            MessageDigest digest = DigestUtils.getSha256Digest();
            digest.update(send.body(), 0, send.body().length);
            String hash = Hex.encodeHexString(digest.digest());
            return hash;
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        return null;
    }



    public static void waitForArgosIntegrationTestServiceToStart() {
        log.info("Waiting for argos integration test service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(30, SECONDS).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getIntegrationTestServiceBaseUrl() + "/actuator/health"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("argos integration test service started");
    }

    public static LinkApi getLinkApi() {
        return getApiClient().buildClient(LinkApi.class);
    }
    
    public static LayoutApi getLayoutApi() {
        return getApiClient().buildClient(LayoutApi.class);
    }

    public static SupplychainApi getSupplychainApi() {
        return getApiClient().buildClient(SupplychainApi.class);
    }

    public static KeyApi getKeyApiApi() {
        return getApiClient().buildClient(KeyApi.class);
    }
    
    public static boolean isValidEndProduct(String json, String supplyChainId) {
        String response = "";
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getApiBaseUrl() + "/api/supplychain/"+supplyChainId+"/verification"))
                        .method("POST", HttpRequest.BodyPublishers.ofString(json))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertThat(send.statusCode(), is(200));
                response = send.body();

                ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);

                assertTrue(node.has("runIsValid"));
                return node.get("runIsValid").asBoolean();
            } catch (IOException | InterruptedException e) {
                fail(e.getMessage());
            }
        
        return false;
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getApiBaseUrl() + "/api");
    }
}
