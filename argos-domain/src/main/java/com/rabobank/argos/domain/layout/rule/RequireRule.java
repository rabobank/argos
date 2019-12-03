package com.rabobank.argos.domain.layout.rule;


import lombok.Builder;

public final class RequireRule extends Rule {

    @Builder
    public RequireRule(String pattern) {
        super(pattern);
    }

}
