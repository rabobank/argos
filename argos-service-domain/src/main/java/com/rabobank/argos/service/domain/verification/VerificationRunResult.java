package com.rabobank.argos.service.domain.verification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationRunResult {
    private boolean runIsValid;

    public static VerificationRunResult notOkay() {
        return VerificationRunResult.builder().runIsValid(false).build();
    }

    public static VerificationRunResult okay() {
        return VerificationRunResult.builder().runIsValid(true).build();
    }
}