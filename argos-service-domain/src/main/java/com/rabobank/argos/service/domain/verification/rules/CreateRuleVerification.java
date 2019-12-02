package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.AllowRule;
import com.rabobank.argos.domain.layout.rule.CreateRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateRuleVerification implements RuleVerification {
    @Override
    public Class<? extends Rule> getRuleClass() {
        return AllowRule.class;
    }

    @Override
    public VerificationRunResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        context.addValidatedArtifacts(context.getFilteredProducts());
        return VerificationRunResult.okay();
    }
}
