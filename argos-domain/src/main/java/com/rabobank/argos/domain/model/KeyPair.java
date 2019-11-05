package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KeyPair {
    private String keyId;
    private byte[] encryptedPrivateKey;
    private byte[] publicKey;
}
