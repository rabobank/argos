package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;

public interface RuleVerification {

    Class<? extends Rule> getRuleClass();

    RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context);

    RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context);

}
