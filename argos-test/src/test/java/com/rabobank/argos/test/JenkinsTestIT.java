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
import com.google.common.base.Optional;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import com.rabobank.argos.argos4j.rest.api.model.RestArtifact;
import com.rabobank.argos.argos4j.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.argos4j.rest.api.model.RestVerifyCommand;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Stream;

import static com.rabobank.argos.test.NexusHelper.getWarSnapshotHash;
import static com.rabobank.argos.test.ServiceStatusHelper.getKeyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.rabobank.argos.test.ServiceStatusHelper.isValidEndProduct;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.rabobank.argos.test.TestServiceHelper.clearDatabase;
import static com.rabobank.argos.test.TestServiceHelper.signAndStoreLayout;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JenkinsTestIT {


    private static final String KEY_PASSWORD = "test";
    private static Properties properties = Properties.getInstance();
    private static final String SERVER_BASEURL = "server.baseurl";
    private static final String TEST_APP_BRANCH = properties.getArgosTestAppBranch();
    private JenkinsServer jenkins;
    private String supplyChainId;
    private String keyIdBob;

    @BeforeAll
    static void startup() {
        log.info("jenkins base url : {}", properties.getJenkinsBaseUrl());
        log.info("Test App branch : {}", TEST_APP_BRANCH);
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        waitForJenkinsToStart();
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        clearDatabase();
        RestSupplyChainItem restSupplyChainItem = getSupplychainApi().createSupplyChain(new RestCreateSupplyChainCommand().name("argos-test-app"));
        this.supplyChainId = restSupplyChainItem.getId();
        RestKeyPair restKeyPair = new ObjectMapper().readValue(getClass().getResourceAsStream("/testmessages/key/keypair1.json"), RestKeyPair.class);
        keyIdBob = restKeyPair.getKeyId();
        getKeyApi().storeKey(restKeyPair);
        RestKeyPair restKeyPairExt = new ObjectMapper().readValue(getClass().getResourceAsStream("/testmessages/key/keypair2.json"), RestKeyPair.class);
        getKeyApi().storeKey(restKeyPairExt);
        restKeyPairExt = new ObjectMapper().readValue(getClass().getResourceAsStream("/testmessages/key/keypair3.json"), RestKeyPair.class);
        getKeyApi().storeKey(restKeyPairExt);
        createLayout();
        jenkins = new JenkinsServer(new URI(properties.getJenkinsBaseUrl()), "admin", "admin");
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

    private void createLayout()  {
        try {
            signAndStoreLayout(supplyChainId,new ObjectMapper().readValue(getClass().getResourceAsStream("/to-verify-layout.json"), RestLayoutMetaBlock.class),keyIdBob, KEY_PASSWORD);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testFreestyle() throws IOException {
        int buildNumber = runBuild(getJob("argos-test-app-freestyle-recording"));
        verifyJobResult(getJob("argos-test-app-freestyle-recording"), buildNumber);
    }

    @Test
    public void testPipeline() throws IOException {
        JobWithDetails pipeLineJob = getJob("argos-test-app-pipeline");
        if (!hasMaster(pipeLineJob)) {
            pipeLineJob.build();
            await().atMost(1, MINUTES).until(() -> hasMaster(pipeLineJob));
        }

        JobWithDetails job = getJob("argos-test-app-pipeline");
        FolderJob folderJob = jenkins.getFolderJob(job).get();
        Map<String, Job> jobs = folderJob.getJobs();
        int buildNumber = runBuild(jobs.get(TEST_APP_BRANCH));

        verifyJobResult(jenkins.getJob(folderJob, TEST_APP_BRANCH), buildNumber);
        
        // a number of times to create a lot of link objects
        verifyJobResult(jenkins.getJob(folderJob, TEST_APP_BRANCH), buildNumber);
        verifyJobResult(jenkins.getJob(folderJob, TEST_APP_BRANCH), buildNumber);

        verifyEndProducts();
    }


    private int runBuild(Job job) throws IOException {
        QueueReference reference = job.build();

        await().atMost(25, SECONDS).until(() -> jenkins.getQueueItem(reference).getExecutable() != null);

        QueueItem queueItem = jenkins.getQueueItem(reference);
        Build build = jenkins.getBuild(queueItem);

        int buildNumber = build.getNumber();

        log.info("build number {}", buildNumber);

        await().atMost(2, MINUTES).until(() -> !build.details().isBuilding());
        return buildNumber;
    }

    private void verifyJobResult(JobWithDetails job, int buildNumber) throws IOException {
        Build build = job.getBuildByNumber(buildNumber);
        if (build.details().getResult() != BuildResult.SUCCESS) {
            Stream.of(build.details().getConsoleOutputText().split("\\r?\\n")).forEach(log::error);
        }
        assertThat(build.details().getResult(), is(BuildResult.SUCCESS));
    }

    public void verifyEndProducts() {
        String hash = getWarSnapshotHash();
        assertTrue(isValidEndProduct(supplyChainId, new RestVerifyCommand().addExpectedProductsItem(new RestArtifact().uri("target/argos-test-app.war").hash(hash))));        
    }

    private JobWithDetails getJob(String name) throws IOException {
        await().atMost(10, SECONDS).until(() -> jenkins.getJob(name) != null);
        return jenkins.getJob(name);
    }

    private boolean hasMaster(JobWithDetails pipeLineJob) throws IOException {
        Optional<FolderJob> folderJob = jenkins.getFolderJob(pipeLineJob);
        return folderJob.isPresent() &&
                folderJob.get().getJob(TEST_APP_BRANCH) != null;
    }
}
