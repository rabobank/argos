package com.rabobank.argos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Artifact implements Serializable {
    private String uri;
    private String hash;
}
