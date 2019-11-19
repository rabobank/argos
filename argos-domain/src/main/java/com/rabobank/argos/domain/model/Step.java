package com.rabobank.argos.domain.model;

import com.rabobank.argos.domain.model.rule.Rule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
public class Step {
    private String stepName;
    private Set<String> authorizedKeyIds;
    private int requiredSignatures;
    private Set<String> expectedCommand;
    private Set<Rule> expectedMaterials;
    private final Set<Rule> expectedProducts;
}
