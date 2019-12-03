package com.rabobank.argos.domain.layout.rule;


import lombok.Builder;

public final class AllowRule extends Rule {
    @Builder
    public AllowRule(String pattern) {
        super(pattern);
    }
}
