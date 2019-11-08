package com.rabobank.argos.test;

import com.intuit.karate.cucumber.KarateStats;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
@Slf4j
public class BaseKarate {
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

    @AfterAll
    private static void generateReport() throws IOException {
        ReportHelper.generateReport();
    }

    public void runTests() {
        KarateStats stats = KarateHelper.runKarateTests(getClass());
        assertEquals(0, stats.getFailCount());
    }

}
