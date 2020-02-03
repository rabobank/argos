package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

class HierarchyRepositoryImplIT {
    private static final String MONGODB_LOCALHOST = "mongodb://localhost:";
    private static final String MONGO_DB = "test";
    private static final String SCAN_PACKAGE = "com.rabobank.argos.service.adapter.out.mongodb.hierarchy";
    private static final String LOCALHOST = "localhost";

    private MongodExecutable mongodExecutable;
    private HierarchyRepository hierarchyRepository;
    private LabelRepository labelRepository;
    private SupplyChainRepository supplyChainRepository;

    private MongoTemplate mongoTemplate;

    @BeforeEach
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
        Mongobee runner = new Mongobee(connectionString);
        runner.setChangeLogsScanPackage(SCAN_PACKAGE);
        runner.setMongoTemplate(mongoTemplate);
        runner.setDbName(MONGO_DB);
        runner.execute();
    }

    @AfterEach
    void clean() {
        mongodExecutable.stop();
    }

    @Test
    void testSearchByName() {
        createDataSet();
        List<TreeNode> result = hierarchyRepository.searchByName("nl");
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().getName(), is("nl"));
    }

    @Test
    void testGetSubTree() {
        createDataSet();
        List<TreeNode> result = hierarchyRepository.searchByName("nl");
        String referenceId = result.iterator().next().getReferenceId();
        Optional<TreeNode> optionalSubTree = hierarchyRepository.getSubTree(referenceId, -1);
        assertThat(optionalSubTree.isPresent(), is(true));
        TreeNode treeNode = optionalSubTree.get();
        assertThat(treeNode.getChildren(), hasSize(1));
        TreeNode company1 = treeNode.getChildren().iterator().next();
        assertThat(company1.getName(), is("company 1"));
        assertThat(company1.getChildren(), hasSize(1));
        TreeNode department1 = company1.getChildren().iterator().next();
        assertThat(department1.getChildren(), hasSize(3));
    }

    void createDataSet() {
        Label root = createLabel("nl", null);
        Label company1 = createLabel("company 1", root.getLabelId());
        Label department1 = createLabel("department 1", company1.getLabelId());
        createLabel("team 3", department1.getLabelId());
        createLabel("team 2", department1.getLabelId());
        Label team1 = createLabel("team 1", department1.getLabelId());
        createSupplyChain("team 1 supply chain", team1.getLabelId());
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