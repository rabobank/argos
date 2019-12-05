package com.rabobank.argos.domain.layout.rule;

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
