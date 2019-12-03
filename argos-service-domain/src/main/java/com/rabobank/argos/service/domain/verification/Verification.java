package com.rabobank.argos.service.domain.verification;

public interface Verification {

    public enum Priority {LAYOUT_METABLOCK_SIGNATURE, LINK_METABLOCK_SIGNATURE, BUILDSTEPS_COMPLETED, EXPECTED_COMMAND}

    Priority getPriority();

    VerificationRunResult verify(VerificationContext context);

}
