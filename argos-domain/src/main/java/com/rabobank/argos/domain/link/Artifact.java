package com.rabobank.argos.domain.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class Artifact implements Serializable {
    private String uri;
    private String hash;
}
