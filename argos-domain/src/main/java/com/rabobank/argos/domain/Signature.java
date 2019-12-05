package com.rabobank.argos.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class Signature {
    private String keyId;
    private String signature;
}
