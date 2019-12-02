package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.CreateRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllowRuleVerification implements RuleVerification {
    @Override
    public Class<? extends Rule> getRuleClass() {
        return CreateRule.class;
    }

    @Override
    public VerificationRunResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        List<Artifact> filteredProducts = context.getFilteredProducts();
        if (!filteredProducts.isEmpty() && context.notContainsMaterials(filteredProducts)) {
            context.addValidatedArtifacts(filteredProducts);
            return VerificationRunResult.okay();
        } else {
            return VerificationRunResult.notOkay();
        }
    }
}
