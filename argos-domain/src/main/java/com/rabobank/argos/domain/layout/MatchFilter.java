package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Setter
@Getter
@Builder
@ToString
public class MatchFilter {

    private String pattern;
    private DestinationType destinationType;
    private String destinationStepName;

    public boolean matchUri(String uri) {
        return pattern.equals(uri);
    }

    public List<Artifact> matches(List<Artifact> productsToVerify) {
        return productsToVerify.stream().filter(artifact -> matchUri(artifact.getUri())).collect(toList());
    }
}
