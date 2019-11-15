package com.rabobank.argos.test;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.junit5.Karate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import static com.rabobank.argos.test.TestHelper.waitForArgosServiceToStart;

@Slf4j
@KarateOptions(tags = {"~@ignore"})
public class ArgosServiceTestIT {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static Properties properties = Properties.getInstance();

    @BeforeAll
    static void setUp() {
        log.info("karate base url : {}", properties.getApiBaseUrl());
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        waitForArgosServiceToStart();
    }

    @Karate.Test
    Karate keypair() {
        return new Karate().feature("classpath:feature/keypair.feature");
    }

    @Karate.Test
    Karate link() {
        return new Karate().feature("classpath:feature/link.feature");
    }

    @Karate.Test
    Karate supplyChain() {
        return new Karate().feature("classpath:feature/supplychain.feature");
    }

}
