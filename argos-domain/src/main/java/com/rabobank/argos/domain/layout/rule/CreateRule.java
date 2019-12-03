package com.rabobank.argos.domain.layout.rule;

import lombok.Builder;

public final class CreateRule extends Rule {
    @Builder
    public CreateRule(String pattern) {
        super(pattern);
    }
}
