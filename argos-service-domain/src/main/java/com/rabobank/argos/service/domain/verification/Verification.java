package com.rabobank.argos.service.domain.verification;

public interface Verification {

    public enum Priority {LAYOUT_METABLOCK_SIGNATURE, LINK_METABLOCK_SIGNATURE, BUILDSTEPS_COMPLETED}

    Priority getPriority();

    VerificationRunResult verify(VerificationContext context);

}
