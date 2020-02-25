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

import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.layout.ArtifactType.PRODUCTS;

import java.util.HashSet;

@Slf4j
@Component
public class MatchRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MATCH;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        MatchRule rule = context.getRule();
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts(rule.getSourcePathPrefix());
        
        String destinationSegmentName = rule.getDestinationSegmentName() != null ? rule.getDestinationSegmentName() : context.getSegmentName();

        Link link = context.getLinkBySegmentNameAndStepName(destinationSegmentName, rule.getDestinationStepName());
        
        if (link != null) {
            Set<Artifact> filteredDestinationArtifacts = null;
            if (rule.getDestinationType() == PRODUCTS) {
                filteredDestinationArtifacts = new HashSet<>(link.getProducts());                
            } else {
                filteredDestinationArtifacts = new HashSet<>(link.getMaterials());
            }
            filteredDestinationArtifacts = ArtifactsVerificationContext.filterArtifacts(filteredDestinationArtifacts, rule.getPattern(), rule.getDestinationPathPrefix());
            if (this.verifyArtifacts(filteredArtifacts, filteredDestinationArtifacts)) {
                context.consume(filteredArtifacts);
                logInfo(log, filteredArtifacts);
                return true;
            } else {
                logErrors(log, filteredArtifacts);
                return false;
            }
        } else {
            log.warn("no link for destination step {}", rule.getDestinationStepName());
            return false;
        }
    }

    private boolean verifyArtifacts(Set<Artifact> filteredSourceArtifacts, Set<Artifact> filteredDestinationArtifacts) {
        return filteredSourceArtifacts
                .stream()
                .map(Artifact::getHash)
                .allMatch(filteredDestinationArtifacts
                        .stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toSet())::contains);
    }    

}
