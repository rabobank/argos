package com.rabobank.argos.domain.layout.rule;


import lombok.Builder;


public final class ModifyRule extends Rule {
    @Builder
    public ModifyRule(String pattern) {
        super(pattern);
    }

}
