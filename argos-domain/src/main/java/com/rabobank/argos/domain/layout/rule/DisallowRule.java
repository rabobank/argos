package com.rabobank.argos.domain.layout.rule;


import lombok.Builder;

public final class DisallowRule extends Rule {
    @Builder
    public DisallowRule(String pattern) {
        super(pattern);
    }

}
