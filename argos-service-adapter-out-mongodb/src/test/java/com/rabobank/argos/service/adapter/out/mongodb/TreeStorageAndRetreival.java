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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

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
    void testPoc() {
        SupplyChainLabel root = createHierarchy();
        SupplyChainNodeUpDater supplyChainNodeUpDater = new SupplyChainNodeUpDater();
        root.accept(supplyChainNodeUpDater);
        assertThat(supplyChainNodeUpDater.result().getCreated(), hasSize(7));
    }

    private SupplyChainNode createBrancheSupplyChain(String name) {
        return SupplyChain
                .builder()
                .name(name)
                .build();
    }

    private SupplyChainLabel createBrancheLabel(String name) {
        return SupplyChainLabel
                .builder()
                .name(name)
                .build();
    }

    private SupplyChainLabel createRoot() {
        return SupplyChainLabel
                .builder()
                .name("root")
                .id("rootId")
                .build();
    }


    private SupplyChainLabel createHierarchy() {
        SupplyChainLabel root = createRoot();
        SupplyChainNode supplyChainNode = createBrancheSupplyChain("supply chain");
        SupplyChainLabel branche1 = createBrancheLabel("branche 1");
        SupplyChainLabel branche2 = createBrancheLabel("branche 2");
        root.addChild(branche1);
        root.addChild(branche2);
        SupplyChainLabel branche1_1 = createBrancheLabel("branche 1_1");
        branche1_1.addChild(supplyChainNode);
        SupplyChainLabel branche1_2 = createBrancheLabel("branche 1_2");
        branche1.addChild(branche1_1);
        branche1.addChild(branche1_2);
        SupplyChainLabel branche2_1 = createBrancheLabel("branche 2_1");
        SupplyChainLabel branche2_2 = createBrancheLabel("branche2_2");
        branche2.addChild(branche2_1);
        branche2.addChild(branche2_2);
        return root;
    }


}
