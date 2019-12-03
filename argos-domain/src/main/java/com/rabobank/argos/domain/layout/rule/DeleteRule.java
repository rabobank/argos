package com.rabobank.argos.domain.layout.rule;

import lombok.Builder;

public final class DeleteRule extends Rule {
    @Builder
    public DeleteRule(String pattern) {
        super(pattern);
    }
}
