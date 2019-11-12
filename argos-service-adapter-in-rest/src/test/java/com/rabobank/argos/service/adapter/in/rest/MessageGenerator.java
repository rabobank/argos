package com.rabobank.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.KeyIdProvider;
import com.rabobank.argos.domain.KeyIdProviderImpl;
import com.rabobank.argos.domain.model.RSAPublicKeyFactory;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MessageGenerator {
    @Test
    @Ignore
    public void generateValidTestKeyPairJson() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        KeyIdProvider keyIdProvider = new KeyIdProviderImpl();
        String keyPairJson = IOUtils.toString(MessageGenerator.class.getResourceAsStream("/keypair.json"), "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        RestKeyPair keyPair = mapper.readValue(keyPairJson, RestKeyPair.class);
        String keyid = keyIdProvider.computeKeyId(RSAPublicKeyFactory.instance(keyPair.getPublicKey()));
        keyPair.setKeyId(keyid);
        mapper.writeValueAsString(keyPair);
        System.out.println(mapper.writeValueAsString(keyPair));
    }

}
