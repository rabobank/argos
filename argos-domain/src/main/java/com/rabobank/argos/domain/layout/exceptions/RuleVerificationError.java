package com.rabobank.argos.domain.layout.exceptions;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;

import java.util.Set;

public class RuleVerificationError extends LayoutVerificationError {

    private static final long serialVersionUID = -6923219476614859770L;

    public RuleVerificationError(Rule rule, Set<Artifact> filteredArtifacts) {
        super(String.format("\'%s [%s]\' matched the following " + "artifacts: [%s]", rule.getClass().getSimpleName(), rule.getPattern(),
                filteredArtifacts));
    }
}
