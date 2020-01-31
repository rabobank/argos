package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static java.nio.charset.StandardCharsets.UTF_8;

class HierarchyRepositoryImplIT {
    private MongodExecutable mongodExecutable;
    private HierarchyRepository hierarchyRepository;
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() throws IOException, MongobeeException {
        String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        String connectionString = "mongodb://localhost:" + port;
        mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        hierarchyRepository = new HierarchyRepositoryImpl(mongoTemplate);
        Mongobee runner = new Mongobee(connectionString);
        runner.setChangeLogsScanPackage("com.rabobank.argos.service.adapter.out.mongodb");
        runner.setMongoTemplate(mongoTemplate);
        runner.setDbName("test");
        runner.execute();
        createDataSet();
    }

    @Test
    void testGetPathToRoot() {
        List<String> pathToRoot = hierarchyRepository.getPathToRoot("5e32e928058a273b0a601168");
    }

    public void createDataSet() {
        try {
            String jsonArray = IOUtils.toString(getClass().getResourceAsStream("/labeldata.json"), UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            List<Label> asList = mapper.readValue(
                    jsonArray, new TypeReference<List<Label>>() {
                    });
            mongoTemplate.insert(asList, "labels");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}