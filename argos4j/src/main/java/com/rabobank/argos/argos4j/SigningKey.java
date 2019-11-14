package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class SigningKey implements Serializable {

    private final byte[] pemKey;
}
