package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.KeyApi;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class TestHelper {

    private static Properties properties = Properties.getInstance();

    public static void clearDatabase() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getApiBaseUrl() + "/integration-test/reset-db"))
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

    public static LinkApi getLinkApi() {
        return getApiClient().buildClient(LinkApi.class);
    }

    public static SupplychainApi getSupplychainApi() {
        return getApiClient().buildClient(SupplychainApi.class);
    }

    public static KeyApi getKeyApiApi() {
        return getApiClient().buildClient(KeyApi.class);
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getApiBaseUrl() + "/api");
    }
}
