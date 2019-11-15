package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.KeyApi;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.KeyIdProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private KeyPair keyPair;

    @BeforeEach
    void reset() throws IOException, InterruptedException, NoSuchAlgorithmException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getApiBaseUrl() + "/integration-test/reset-db"))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(send.statusCode(), is(200));
    }

    @Test
    void postLinkMetaBlockWithSignatureValidation() {

        PublicKey publicKey = keyPair.getPublic();
        String keyId = new KeyIdProviderImpl().computeKeyId(publicKey);

        ApiClient apiClient = new ApiClient();
        KeyApi keyApi = apiClient.buildClient(KeyApi.class);
        keyApi.storeKey(new RestKeyPair().keyId(keyId).publicKey(publicKey.getEncoded()));

        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("http://localhost:" + 8080 + "/api")
                .stepName("build")
                .supplyChainId("supplyChainId")
                .signingKey(SigningKey.builder()
                        .keyPair(keyPair).build())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        argos4j.collectProducts(new File("."));
        argos4j.collectMaterials(new File("."));
        argos4j.store();
    }
}
