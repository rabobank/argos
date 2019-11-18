package com.rabobank.argos.domain.exceptions;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.rule.Rule;

import java.util.Set;

public class RuleVerificationError extends LayoutVerificationError {

    private static final long serialVersionUID = -6923219476614859770L;

    public RuleVerificationError(Rule rule, Set<Artifact> filteredArtifacts) {
        super(String.format("\'%s [%s]\' matched the following " + "artifacts: [%s]", rule.getClass().getSimpleName(), rule.getPattern(),
                filteredArtifacts));
    }
}
