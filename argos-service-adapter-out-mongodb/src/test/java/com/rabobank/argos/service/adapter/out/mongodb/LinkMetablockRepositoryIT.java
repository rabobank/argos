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
package com.rabobank.argos.service.adapter.out.mongodb;

import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class LinkMetablockRepositoryIT {

    private static final String STEP_NAME = "stepName";
    private static final String SUPPLYCHAIN = "supplychain";
    private static final String HASH_1 = "74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63";
    private static final String HASH_2 = "1e6a4129c8b90e9b6c4727a59b1013d714576066ad1bad05034847f30ffb62b6";
    private static final String ARGOS_TEST_IML = "argos-test.iml";
    private static final String DOCKER_1_IML = "docker (1).iml";
    public static final String RUN_ID = "runId";
    private static final String SEGMENT_NAME = "segmentName";
    private MongodExecutable mongodExecutable;
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @BeforeEach
    void setup() throws IOException {
        String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:" + port), "test");
        linkMetaBlockRepository = new LinkMetaBlockRepositoryImpl(mongoTemplate);
    }

    @AfterEach
    void clean() {
        mongodExecutable.stop();
    }

    @Test
    void findByRunIdShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, RUN_ID);
        assertThat(links, hasSize(1));
    }

    @Test
    void findByRunIdWithSegmentNameAndResolvedStepShouldNotRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, SEGMENT_NAME, RUN_ID, singletonList(STEP_NAME));
        assertThat(links, hasSize(0));
    }

    @Test
    void findByRunIdWithSegmentNameShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, SEGMENT_NAME, RUN_ID, new ArrayList<>());
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHashesShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMultipleProductHashesShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, HASH_2));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHashesShouldNotRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, "INCORRECT_HASH"));
        assertThat(links, hasSize(0));
    }


    @Test
    void findBySupplyChainAndStepNameAndMaterialsHashesShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMultipleMaterialsHashesShouldRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, HASH_2));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMaterialsHashesShouldNotRetreive() {
        loadData();
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, "INCORRECT_HASH"));
        assertThat(links, hasSize(0));
    }


    private void loadData() {
        LinkMetaBlock linkmetaBlock = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLYCHAIN)
                .signature(createSignature())
                .link(createLink())
                .build();
        linkMetaBlockRepository.save(linkmetaBlock);
    }

    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .signature("signature")
                .build();
    }

    private Link createLink() {
        return Link
                .builder()
                .runId(RUN_ID)
                .layoutSegmentName(SEGMENT_NAME)
                .stepName(STEP_NAME)
                .materials(createMaterials())
                .products(createProducts())
                .build();
    }

    private List<Artifact> createMaterials() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }

    private List<Artifact> createProducts() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }
}
