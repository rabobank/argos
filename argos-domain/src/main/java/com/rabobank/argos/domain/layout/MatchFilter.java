package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Setter
@Getter
@ToString
public class MatchFilter {

    private String pattern;
    private DestinationType destinationType;
    private String destinationStepName;


    @Builder
    public MatchFilter(String pattern, DestinationType destinationType, String destinationStepName) {
        this.pattern = pattern;
        this.destinationType = destinationType;
        this.destinationStepName = destinationStepName;
    }

    public boolean matchUri(String uri) {
        return FileSystems.getDefault().getPathMatcher("glob:" + pattern).matches(Paths.get(uri));
    }

    public List<Artifact> matches(List<Artifact> productsToVerify) {
        return productsToVerify.stream().filter(artifact -> matchUri(artifact.getUri())).collect(toList());
    }


}
