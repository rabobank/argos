package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SigningKey {

    private SigningKeyType type;

    private final byte[] key;
}
