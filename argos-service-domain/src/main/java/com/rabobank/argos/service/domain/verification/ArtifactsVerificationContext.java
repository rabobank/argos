/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class ArtifactsVerificationContext {
    
    @NonNull
    private ArtifactType type;

    @NonNull
    private final Map<String, Map<String, Link>> linksMap;
    
    @NonNull
    private final Link link;
    
    @NonNull
    private Set<Artifact> notConsumedArtifacts;
    
    @Builder.Default
    private Set<Artifact> consumedArtifacts = new HashSet<>();
    
    @NonNull
    private final String segmentName;

    @NonNull
    private final Step step;

    public Set<Artifact> getFilteredArtifacts(String pattern) {
        return getFilteredArtifacts(pattern, null);
    }

    public Set<Artifact> getFilteredArtifacts(String pattern, String prefix) {
        return filterArtifacts(notConsumedArtifacts, pattern, prefix);
    }

    public static Set<Artifact> filterArtifacts(Set<Artifact> artifacts, String pattern, @Nullable String prefix) {
        return artifacts.stream().filter(artifact -> ArtifactMatcher.matches(getUri(artifact, prefix), pattern)).collect(Collectors.toSet());
    }

    private static String getUri(Artifact artifact, String prefix) {
        if (StringUtils.hasLength(prefix) && artifact.getUri().startsWith(prefix)) {
            return Paths.get(prefix).relativize(Paths.get(artifact.getUri())).toString();
        } else {
            return artifact.getUri();
        }
    }
    
    public Optional<Link> getLinkBySegmentNameAndStepName(String segmentName, String stepName) {
        return Optional.ofNullable(linksMap.get(segmentName).get(stepName));
    }
 
    public Link getLink() {
        return this.link;
    }

    public Set<Artifact> getMaterials() {
        return new HashSet<>(this.getLink().getMaterials());
    }
    
    public Set<Artifact> getProducts() {
        return new HashSet<>(this.getLink().getProducts());
    }
    
    public void consume(Set<Artifact> artifacts) {
        notConsumedArtifacts.removeAll(artifacts);
        consumedArtifacts.addAll(artifacts);
    }
}
