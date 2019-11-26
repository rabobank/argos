package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.JsonSigningSerializer;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.layout.LayoutMetaBlockMapper;
import com.rabobank.argos.service.adapter.in.rest.link.LinkMetaBlockMapper;
import com.rabobank.argos.service.domain.RepositoryResetProvider;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.context.annotation.Profile;
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

@Profile("integration-test")
@RequestMapping("/integration-test")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestITService {

    private final RepositoryResetProvider repositoryResetProvider;

    private final LayoutMetaBlockMapper layoutMetaBlockMapper;

    private final LinkMetaBlockMapper linkMetaBlockMapper;

    private final KeyPairRepository keyPairRepository;

    @PostMapping(value = "/reset-db")
    public void resetDatabase() {
        log.info("resetDatabase");
        repositoryResetProvider.resetAllRepositories();
    }

    @PostMapping(value = "/signLayoutMetaBlock")
    public ResponseEntity<RestLayoutMetaBlock> signLayout(@RequestBody RestLayoutMetaBlock restLayoutMetaBlock) {
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockMapper.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        KeyPair keyPair = generateKeyPair();
        String keyId = storePublicKey(keyPair);
        String signature = createSignature(keyPair.getPrivate(), new JsonSigningSerializer().serialize(layoutMetaBlock.getLayout()));
        layoutMetaBlock.setSignatures(Collections.singletonList(Signature.builder().signature(signature).keyId(keyId).build()));
        return ResponseEntity.ok(layoutMetaBlockMapper.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

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
