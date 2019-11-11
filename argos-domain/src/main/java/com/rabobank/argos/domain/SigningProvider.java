package com.rabobank.argos.domain;

import java.security.PublicKey;

public interface SigningProvider {
    String computeKeyId(PublicKey publicKey);
}
