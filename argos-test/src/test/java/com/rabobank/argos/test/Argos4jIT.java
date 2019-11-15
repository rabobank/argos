package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.argos4j.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.KeyIdProviderImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static com.rabobank.argos.test.TestHelper.clearDatabase;
import static com.rabobank.argos.test.TestHelper.getKeyApiApi;
import static com.rabobank.argos.test.TestHelper.getSupplychainApi;
import static com.rabobank.argos.test.TestHelper.waitForArgosServiceToStart;

public class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private KeyPair keyPair;

    @BeforeAll
    static void setUp() {
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void reset() throws NoSuchAlgorithmException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        clearDatabase();
    }

    @Test
    void postLinkMetaBlockWithSignatureValidation() {

        PublicKey publicKey = keyPair.getPublic();
        String keyId = new KeyIdProviderImpl().computeKeyId(publicKey);

        getKeyApiApi().storeKey(new RestKeyPair().keyId(keyId).publicKey(publicKey.getEncoded()));
        getSupplychainApi().createSupplyChain(new RestCreateSupplyChainCommand().name("test-supply-chain"));

        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .stepName("build")
                .supplyChainName("test-supply-chain")
                .signingKey(SigningKey.builder()
                        .keyPair(keyPair).build())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        argos4j.collectProducts(new File("."));
        argos4j.collectMaterials(new File("."));
        argos4j.store();
    }
}
