package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
@Builder
public class MatchFilter {

    private String pattern;
    private DestinationType destinationType;
    private String destinationStepName;

    public List<Artifact> applyFilter(List<Artifact> productsToVerify) {
        return Collections.emptyList();
    }

    public boolean matchUri(String uri) {
        return false;
    }
}
