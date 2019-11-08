package com.rabobank.argos.test;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ArgosTestIT {

    private static HttpClient client;

    private static Properties properties = Properties.getInstance();

    @BeforeAll
    public static void waitForServiceToStart(){
        log.info("Waiting for argos service start");
        client = HttpClient.newHttpClient();
        await().atMost(10, SECONDS).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getApiBaseUr()+"/actuator/health"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e){
                //ignore
                return false;
            }
        });

        log.info("argos service started");
    }



    @Test
    public void helloWorld() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getApiBaseUr()))
                .build();
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200,send.statusCode());
    }
}
