package com.rabobank.argos.domain.layout.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class Rule {
    private String pattern;
}
