package com.rabobank.argos.domain.layout.rule;


import com.rabobank.argos.domain.layout.DestinationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class MatchRule extends Rule {

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


}


