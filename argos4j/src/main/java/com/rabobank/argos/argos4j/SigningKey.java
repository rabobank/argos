package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.security.KeyPair;

@Getter
@Builder
public class SigningKey implements Serializable {
    private final KeyPair keyPair;
}
