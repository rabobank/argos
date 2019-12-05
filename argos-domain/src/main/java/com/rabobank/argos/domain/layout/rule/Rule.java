package com.rabobank.argos.domain.layout.rule;

import com.rabobank.argos.domain.layout.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.link.Artifact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Rule {
    private RuleType ruleType;
    private String pattern;
}
