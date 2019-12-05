package com.rabobank.argos.domain.key;

import java.security.PublicKey;

public interface KeyIdProvider {
    String computeKeyId(PublicKey publicKey);
}
