package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;

@Builder
@Getter
@Setter
public class KeyPair {
    private String keyId;
    private byte[] encryptedPrivateKey;
    private PublicKey publicKey;
}
