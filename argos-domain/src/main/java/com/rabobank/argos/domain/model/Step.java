package com.rabobank.argos.domain.model;

import com.rabobank.argos.domain.model.rule.Rule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Step {
    private String stepName;
    private List<String> authorizedKeyIds;
    private int requiredNumberOfLinks;
    private List<String> expectedCommand;
    private List<Rule> expectedMaterials;
    private List<Rule> expectedProducts;
}
