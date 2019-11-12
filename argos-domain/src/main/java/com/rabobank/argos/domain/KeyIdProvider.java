package com.rabobank.argos.domain;

import java.security.PublicKey;

public interface KeyIdProvider {
    String computeKeyId(PublicKey publicKey);
}
