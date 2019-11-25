package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Link {
    private String runId;
    private String stepName;
    private List<String> command;
    private List<Artifact> materials;
    private List<Artifact> products;
}
