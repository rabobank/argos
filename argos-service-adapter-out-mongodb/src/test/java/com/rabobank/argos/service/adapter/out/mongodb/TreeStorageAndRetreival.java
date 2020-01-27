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

import de.flapdoodle.embed.mongo.MongodExecutable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TreeStorageAndRetreival {
    private MongodExecutable mongodExecutable;

    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() throws IOException {
       /* String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:" + port), "test");*/
    }

    @AfterEach
    void clean() {/*
        mongodExecutable.stop();*/
    }


    @Test
    void test() {
        SupplyChainLabel root = createHierarchy();

        SupplyChainNode supplyChainNode = SupplyChain
                .builder()
                .name("supplyChain leaf new")
                .build();

        SupplyChainNodeInserter supplyChainNodeInserter = SupplyChainNodeInserter
                .builder()
                .nodeToInsert(supplyChainNode)
                .parentRef("branche1_1ID")
                .build();

        SupplyChainNodeUpDater supplyChainNodeUpDater = new SupplyChainNodeUpDater();

        root.accept(supplyChainNodeInserter);
        root.accept(supplyChainNodeUpDater);
        assertThat(supplyChainNodeInserter.result(), is(true));
    }

    private SupplyChainLabel createHierarchy() {
        SupplyChainLabel root = SupplyChainLabel
                .builder()
                .name("root")
                .id("rootId")
                .build();

        SupplyChainLabel branche1 = SupplyChainLabel
                .builder()

                .name("branche 1")
                .id("branche1ID")
                .build();

        SupplyChainLabel branche2 = SupplyChainLabel
                .builder()

                .name("branche 2")
                .id("branche2ID")

                .build();
        root.addChild(branche1);
        root.addChild(branche2);

        SupplyChainLabel branche1_1 = SupplyChainLabel
                .builder()
                .name("branche1_1")
                .id("branche1_1ID")
                .build();

        SupplyChainLabel branche1_2 = SupplyChainLabel
                .builder()
                .name("branche1_2")
                .id("branche1_2ID")
                .build();

        branche1.addChild(branche1_1);
        branche1.addChild(branche1_2);

        SupplyChainLabel branche2_1 = SupplyChainLabel
                .builder()
                .name("branche2_1")
                .id("branche2_1ID")
                .build();

        SupplyChainLabel branche2_2 = SupplyChainLabel
                .builder()
                .name("branche2_2")
                .id("branche2_2ID")
                .build();
        branche2.addChild(branche2_1);
        branche2.addChild(branche2_2);
        return root;
    }


}
