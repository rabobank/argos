package com.rabobank.argos.service.adapter.out.mongodb;

import de.flapdoodle.embed.mongo.MongodExecutable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;

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
        Label root = createRoot();
        SupplyChainNode supplyChainNode = SupplyChain
                .builder()
                .name("supplyChain")
                .id("id").build();

        SupplyChainNodeInserter supplyChainNodeInserter = SupplyChainNodeInserter.builder()
                .nodeToInsert(supplyChainNode)
                .parentRef("rootSubSubTree2")
                .build();

        root.accept(supplyChainNodeInserter);
        assertThat(supplyChainNodeInserter.result(), is(true));
    }

    private Label createRoot() {
        Label root = Label
                .builder()
                .idPathToRoot(List.of("rootId"))
                .namePathToRoot(List.of("root"))
                .name("root")
                .id("rootId")
                .lft(1)
                .rght(2)
                .depth(1)
                .build();

        Label rootSubTree1 = Label
                .builder()
                .idPathToRoot(List.of("rootSubTree1"))
                .namePathToRoot(List.of("rootSubTree one"))
                .name("rootSubTree one")
                .id("rootSubTree1")
                .lft(1)
                .rght(2)
                .depth(1)
                .build();

        Label rootSubSubTree1 = Label
                .builder()
                .idPathToRoot(List.of("rootSubTree1"))
                .namePathToRoot(List.of("rootSubTree one"))
                .name("rootSubTree one")
                .id("rootSubTree1")
                .lft(1)
                .rght(2)
                .depth(1)
                .build();

        rootSubTree1.addChild(rootSubSubTree1);

        Label rootSubSubTree2 = Label
                .builder()
                .idPathToRoot(List.of("rootSubSubTree2"))
                .namePathToRoot(List.of("rootSubSubTree two"))
                .name("rootSubSubTree two")
                .id("rootSubSubTree2")
                .lft(1)
                .rght(2)
                .depth(1)
                .build();
        rootSubTree1.addChild(rootSubSubTree2);

        Label rootSubTree2 = Label
                .builder()
                .idPathToRoot(List.of("rootSubTree2"))
                .namePathToRoot(List.of("rootSubTree Two"))
                .name("rootSubTree one")
                .id("rootSubTree1")
                .lft(1)
                .rght(2)
                .depth(1)
                .build();

        root.addChild(rootSubTree1);
        root.addChild(rootSubTree2);
        return root;
    }


}
