package com.rabobank.argos.service.domain.verification;

public interface Verification {

    enum Priority {LAYOUT_METABLOCK_SIGNATURE, LINK_METABLOCK_SIGNATURE, BUILDSTEPS_COMPLETED, RULES}

    Priority getPriority();

    VerificationRunResult verify(VerificationContext context);

}
