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
package com.rabobank.argos.service.domain.verification.rules;

import java.util.Set;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RuleVerificationContext<R extends Rule> {
    
    @NonNull
    private ArtifactsVerificationContext artifactsContext;
    
    @NonNull
    private final R rule;
    
    public Set<Artifact> getFilteredArtifacts() {
        return artifactsContext.getFilteredArtifacts(rule.getPattern(), null);
    }

    public Set<Artifact> getFilteredArtifacts(String prefix) {
        return artifactsContext.getFilteredArtifacts(rule.getPattern(), prefix);
    }
    
    public void consume(Set<Artifact> artifacts) {
        artifactsContext.consume(artifacts);
    }
    
    public String getSegmentName() {
        return artifactsContext.getSegmentName();
    }
    
    public Link getLinkBySegmentNameAndStepName(String segmentName, String stepName) {
        return artifactsContext.getLinkBySegmentNameAndStepName(segmentName, stepName);
    }
    
    public Set<Artifact> getMaterials() {
        return artifactsContext.getMaterials();
    }
    
    public Set<Artifact> getProducts() {
        return artifactsContext.getProducts();
    }

    public <T extends Rule> T getRule() {
        return (T) rule;
    }
}
