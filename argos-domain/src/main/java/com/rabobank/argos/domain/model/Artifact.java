package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Artifact {
    private String uri;

    private HashAlgorithm hashAlgorithm;

    private String hash;
}
