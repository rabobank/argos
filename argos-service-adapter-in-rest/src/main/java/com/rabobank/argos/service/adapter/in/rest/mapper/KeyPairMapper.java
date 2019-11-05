package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@Mapper(componentModel = "spring")
public interface KeyPairMapper {
    KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);
    RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    @Mapping(source = "publicKey", target = "publicKey")
    default PublicKey convertByteArrayToPublicKey(byte[] publicKey) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}
