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
package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl;
import com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl;
import com.rabobank.argos.service.domain.account.NonPersonalAccountRepository;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HierarchyRepositoryImplIT {
    private static final String MONGODB_LOCALHOST = "mongodb://localhost:";
    private static final String MONGO_DB = "test";
    private static final String SCAN_PACKAGE = "com.rabobank.argos.service.adapter.out.mongodb.hierarchy";
    private static final String LOCALHOST = "localhost";

    private MongodExecutable mongodExecutable;
    private HierarchyRepository hierarchyRepository;
    private LabelRepository labelRepository;
    private SupplyChainRepository supplyChainRepository;
    private NonPersonalAccountRepository nonPersonalAccountRepository;

    private MongoTemplate mongoTemplate;

    @BeforeAll
    void setup() throws IOException, MongobeeException {
        String ip = LOCALHOST;
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        String connectionString = MONGODB_LOCALHOST + port;
        mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), MONGO_DB);
        hierarchyRepository = new HierarchyRepositoryImpl(mongoTemplate);
        labelRepository = new LabelRepositoryImpl(mongoTemplate);
        supplyChainRepository = new SupplyChainRepositoryImpl(mongoTemplate);
        nonPersonalAccountRepository = new NonPersonalAccountRepositoryImpl(mongoTemplate);
        Mongobee runner = new Mongobee(connectionString);
        runner.setChangeLogsScanPackage(SCAN_PACKAGE);
        runner.setMongoTemplate(mongoTemplate);
        runner.setDbName(MONGO_DB);
        runner.execute();
        createDataSet();
    }

    @AfterAll
    void clean() {
        mongodExecutable.stop();
    }

    @Test
    void testGetRootNodes() {
        List<TreeNode> result = hierarchyRepository.getRootNodes(HierarchyMode.NONE, 0);
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().getName(), is("nl"));
        assertThat(result.iterator().next().isHasChildren(), is(true));
    }

    @Test
    void testFindByNamePathToRootAndType() {
        List<String> pathToRoot = List.of("team 1", "department 1", "company 1", "nl");
        Optional<TreeNode> optionalSubTree = hierarchyRepository
                .findByNamePathToRootAndType("team 1 supply chain", pathToRoot, TreeNode.Type.SUPPLY_CHAIN);
        assertThat(optionalSubTree.isPresent(), is(true));
        assertThat(optionalSubTree.get().getName(), is("team 1 supply chain"));
        assertThat(optionalSubTree.get().getType(), is(TreeNode.Type.SUPPLY_CHAIN));
    }

    @Test
    void testGetSubTree() {
        List<TreeNode> result = hierarchyRepository.getRootNodes(HierarchyMode.NONE, 0);
        String referenceId = result.iterator().next().getReferenceId();
        Optional<TreeNode> optionalSubTree = hierarchyRepository.getSubTree(referenceId, HierarchyMode.ALL, 0);
        assertThat(optionalSubTree.isPresent(), is(true));
        TreeNode treeNode = optionalSubTree.get();
        assertThat(treeNode.getChildren(), hasSize(1));
        TreeNode company1 = treeNode.getChildren().iterator().next();
        assertThat(company1.getName(), is("company 1"));
        assertThat(company1.getChildren(), hasSize(1));
        TreeNode department1 = company1.getChildren().iterator().next();
        assertThat(department1.getChildren(), hasSize(3));
        TreeNode team1 = department1.getChildren().iterator().next();
        assertThat(team1.getName(), is("team 1"));
        assertThat(team1.getChildren(), hasSize(3));
        TreeNode npa = team1.getChildren().iterator().next();
        assertThat(npa.getName(), is("team 1 npa 1"));
        assertThat(npa.getType(), is(TreeNode.Type.NON_PERSONAL_ACCOUNT));
    }

    void createDataSet() {
        Label root = createLabel("nl", null);
        Label company1 = createLabel("company 1", root.getLabelId());
        Label department1 = createLabel("department 1", company1.getLabelId());
        createLabel("team 3", department1.getLabelId());
        createLabel("team 2", department1.getLabelId());
        Label team1 = createLabel("team 1", department1.getLabelId());
        createLabel("team 1 supply chain", team1.getLabelId());
        createNonPersonalAccount("team 1 npa 1", team1.getLabelId());
        createSupplyChain("team 1 supply chain", team1.getLabelId());
    }

    private void createNonPersonalAccount(String name, String parentLabelId) {
        nonPersonalAccountRepository.save(NonPersonalAccount.builder().name(name).parentLabelId(parentLabelId).build());
    }

    private Label createLabel(String name, String parentId) {
        Label label = Label.builder()
                .name(name)
                .parentLabelId(parentId)
                .build();
        labelRepository.save(label);
        return label;
    }

    private SupplyChain createSupplyChain(String name, String parentId) {
        SupplyChain supplyChain = SupplyChain.builder()
                .name(name)
                .parentLabelId(parentId)
                .build();
        supplyChainRepository.save(supplyChain);
        return supplyChain;
    }
}