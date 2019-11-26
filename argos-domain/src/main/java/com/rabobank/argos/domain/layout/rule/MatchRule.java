package com.rabobank.argos.domain.layout.rule;


import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Setter
@Getter
public final class MatchRule extends Rule {

    public enum DestinationType {
        PRODUCTS, MATERIALS

    }

    private String sourcePathPrefix;
    private String destinationPathPrefix;
    private DestinationType destinationType;
    private String destinationStepName;

    @Builder
    public MatchRule(String pattern, String destinationPathPrefix, String sourcePathPrefix, DestinationType destinationType,
                     String destinationStepName) {
        super(pattern);
        this.sourcePathPrefix = sourcePathPrefix;
        this.destinationPathPrefix = destinationPathPrefix;
        this.destinationType = destinationType;
        this.destinationStepName = destinationStepName;
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        return Collections.emptySet();
    }

}


