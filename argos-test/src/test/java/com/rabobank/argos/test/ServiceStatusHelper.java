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
package com.rabobank.argos.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.HierarchyApi;
import com.rabobank.argos.argos4j.rest.api.client.LayoutApi;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.NonPersonalAccountApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.client.VerificationApi;
import com.rabobank.argos.argos4j.rest.api.model.RestInlineResponse200;
import com.rabobank.argos.argos4j.rest.api.model.RestVerifyCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class ServiceStatusHelper {

    private static Properties properties = Properties.getInstance();

    public static void waitForArgosServiceToStart() {
        log.info("Waiting for argos service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(1, MINUTES).until(() -> {
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

    public static LinkApi getLinkApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(LinkApi.class);
    }

    public static SupplychainApi getSupplychainApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(SupplychainApi.class);
    }

    public static String getToken() {
        HttpGet request = new HttpGet(properties.getApiBaseUrl() + "/api/oauth2/authorize/azure?redirect_uri=/authenticated");
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            return new ObjectMapper().readValue(response.getEntity().getContent(), RestInlineResponse200.class).getToken();
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    public static HierarchyApi getHierarchyApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(HierarchyApi.class);
    }

    public static boolean isValidEndProduct(String bearerToken, String supplyChainId, RestVerifyCommand verifyCommand) {
        return getVerificationApi(bearerToken).performVerification(supplyChainId, verifyCommand).getRunIsValid();
    }

    public static NonPersonalAccountApi getNonPersonalAccountApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(NonPersonalAccountApi.class);
    }

    public static VerificationApi getVerificationApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(VerificationApi.class);
    }

    public static LayoutApi getLayoutApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(LayoutApi.class);
    }

    private static ApiClient getApiClient(String bearerToken) {
        ApiClient apiClient = new ApiClient("bearerAuth").setBasePath(properties.getApiBaseUrl() + "/api");
        apiClient.setBearerToken(bearerToken);
        return apiClient;
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getApiBaseUrl() + "/api");
    }

}
