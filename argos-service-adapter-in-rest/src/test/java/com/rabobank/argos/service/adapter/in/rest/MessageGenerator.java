package com.rabobank.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.KeyPair;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

public class MessageGenerator {

    @Test
   public void generateValidTestKeyPairJson() throws IOException {

        //SigningProvider signingProvider = new SigningProviderImpl();
        String keyPairJson = IOUtils.toString(MessageGenerator.class.getResourceAsStream("/keypair.json"), "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        KeyPair keyPair =  new ObjectMapper().readValue(keyPairJson, KeyPair.class);

    }

}
