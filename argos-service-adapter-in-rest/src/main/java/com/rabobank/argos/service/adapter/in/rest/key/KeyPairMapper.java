package com.rabobank.argos.service.adapter.in.rest.key;

import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static com.rabobank.argos.domain.key.RSAPublicKeyFactory.instance;

@Mapper(componentModel = "spring")
public interface KeyPairMapper {

    KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);

    RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    @Mapping(source = "publicKey", target = "publicKey")
    default PublicKey convertByteArrayToPublicKey(byte[] publicKey) {
        try {
            return instance(publicKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid public key " + e.getMessage());
        }
    }

    @Mapping(source = "publicKey", target = "publicKey")
    default byte[] convertPublicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
