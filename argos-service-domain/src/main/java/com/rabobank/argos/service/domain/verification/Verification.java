package com.rabobank.argos.service.domain.verification;

public interface Verification {

    int getPriority();

    VerificationRunResult verify(VerificationContext context);

}
