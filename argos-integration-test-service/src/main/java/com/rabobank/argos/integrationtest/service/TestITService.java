/*
 * Copyright (C) 2019 Rabobank Nederland
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
package com.rabobank.argos.integrationtest.service;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.JsonSigningSerializer;
import com.rabobank.argos.integrationtest.argos.service.api.handler.IntegrationTestServiceApi;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestLinkMetaBlock;
import com.rabobank.argos.integrationtest.service.layout.LayoutMetaBlockMapper;
import com.rabobank.argos.integrationtest.service.link.LinkMetaBlockMapper;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Collections;

@RequestMapping("/integration-test")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestITService implements IntegrationTestServiceApi {

    private final RepositoryResetProvider repositoryResetProvider;

    private final LayoutMetaBlockMapper layoutMetaBlockMapper;

    private final LinkMetaBlockMapper linkMetaBlockMapper;

    private final KeyPairRepository keyPairRepository;

    @PostMapping(value = "/reset-db")
    @Override
    public ResponseEntity<Void> resetDatabase() {
        log.info("resetDatabase");
        repositoryResetProvider.resetAllRepositories();
        return null;
    }

    @Override
    @PostMapping(value = "/signLayoutMetaBlock")
    public ResponseEntity<RestLayoutMetaBlock> signLayout(@RequestBody RestLayoutMetaBlock restLayoutMetaBlock) {
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockMapper.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        KeyPair keyPair = generateKeyPair();
        String keyId = storePublicKey(keyPair);
        layoutMetaBlock.getLayout().setAuthorizedKeyIds(Collections.singletonList(keyId));
        String signature = createSignature(keyPair.getPrivate(), new JsonSigningSerializer().serialize(layoutMetaBlock.getLayout()));
        layoutMetaBlock.setSignatures(Collections.singletonList(Signature.builder().signature(signature).keyId(keyId).build()));
        return ResponseEntity.ok(layoutMetaBlockMapper.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    @PostMapping(value = "/signLinkMetaBlock")
    public ResponseEntity<RestLinkMetaBlock> signLink(@RequestBody RestLinkMetaBlock restLinkMetaBlock) {
        LinkMetaBlock linkMetaBlock = linkMetaBlockMapper.convertFromRestLinkMetaBlock(restLinkMetaBlock);
        KeyPair keyPair = generateKeyPair();
        String keyId = storePublicKey(keyPair);
        String signature = createSignature(keyPair.getPrivate(), new JsonSigningSerializer().serialize(linkMetaBlock.getLink()));
        linkMetaBlock.setSignature(Signature.builder().signature(signature).keyId(keyId).build());
        return ResponseEntity.ok(linkMetaBlockMapper.convertToRestLinkMetaBlock(linkMetaBlock));

    }

    private String storePublicKey(KeyPair keyPair) {
        String keyId = new KeyIdProviderImpl().computeKeyId(keyPair.getPublic());
        log.info("storing public key with id {}", keyId);
        keyPairRepository.save(com.rabobank.argos.domain.key.KeyPair.builder().keyId(keyId).publicKey(keyPair.getPublic()).build());
        return keyId;
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private String createSignature(PrivateKey privateKey, String jsonRepr) {
        try {
            java.security.Signature privateSignature = java.security.Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(privateSignature.sign());
        } catch (GeneralSecurityException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
