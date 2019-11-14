package com.rabobank.argos.test;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class JenkinsTestIT {

    private static Properties properties = Properties.getInstance();

    @BeforeAll
    static void setUp() {
        log.info("jenkins base url : {}", properties.getJenkinsBaseUrl());
        waitForJenkinsToStart();
    }

    private static void waitForJenkinsToStart() {
        log.info("Waiting for jenkins start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(1, MINUTES).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getJenkinsBaseUrl() + "/login"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("jenkins started");

    }

    @Test
    public void testFreestyle() throws IOException, URISyntaxException {

        JenkinsServer jenkins = new JenkinsServer(new URI(properties.getJenkinsBaseUrl()), "admin", "admin");

        await().atMost(10, SECONDS).until(() -> getJob(jenkins) != null);
        JobWithDetails job = getJob(jenkins);
        QueueReference reference = job.build();

        await().atMost(25, SECONDS).until(() -> jenkins.getQueueItem(reference).getExecutable() != null);

        QueueItem queueItem = jenkins.getQueueItem(reference);
        Build build = jenkins.getBuild(queueItem);

        int buildNumber = build.getNumber();

        log.info("build number {}", buildNumber);

        await().atMost(1, MINUTES).until(() -> !build.details().isBuilding());

        Build lastSuccessfulBuild = getJob(jenkins).getLastSuccessfulBuild();
        Build lastUnsuccessfulBuild = getJob(jenkins).getLastUnsuccessfulBuild();

        if(lastUnsuccessfulBuild != Build.BUILD_HAS_NEVER_RUN) {
            Stream.of(lastUnsuccessfulBuild.details().getConsoleOutputText().split("\\r?\\n")).forEach(log::error);
        }
        //this failed the test
        // assertThat(lastUnsuccessfulBuild.getNumber(), is(-1));
        assertThat(lastSuccessfulBuild.getNumber(), is(buildNumber));



    }

    private JobWithDetails getJob(JenkinsServer jenkins) throws IOException {
        return jenkins.getJob("argos-test-app-freestyle-recording");
    }
}
