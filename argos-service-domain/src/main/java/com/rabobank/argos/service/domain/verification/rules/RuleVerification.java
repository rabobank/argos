package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;

public interface RuleVerification {

    Class<? extends Rule> getRuleClass();

    VerificationRunResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context);

}
