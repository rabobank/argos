package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.language.bm.Rule;

import java.util.Set;

@Builder
@Getter
@Setter
public class Step {
    private Set<String> authorizedKeyIds;
    private int requiredSignatures;
    private Set<String> expectedCommand;
    private Set<Rule> expectedMaterials;
}
