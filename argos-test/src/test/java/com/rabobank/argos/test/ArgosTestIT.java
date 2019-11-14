package com.rabobank.argos.test;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.junit5.Karate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@Slf4j
@KarateOptions(tags = {"~@ignore"})
public class ArgosTestIT {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static Properties properties = Properties.getInstance();

    @BeforeAll
    static void setUp() {
        log.info("karate base url : {}", properties.getApiBaseUrl());
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        waitForServiceToStart();
    }

    private static void waitForServiceToStart() {
        log.info("Waiting for argos service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(10, SECONDS).until(() -> {
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


    @Karate.Test
    Karate keypair() {
        return new Karate().feature("classpath:feature/keypair.feature");
    }

    @Karate.Test
    Karate link() {
        return new Karate().feature("classpath:feature/link.feature");
    }

    @Karate.Test
    Karate supplyChain() {
        return new Karate().feature("classpath:feature/supplychain.feature");
    }

}
