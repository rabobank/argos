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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.layout.ArtifactType.PRODUCTS;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

@Slf4j
@Component
public class MatchRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MATCH;
    }

    @Override
    public Boolean verify(RuleVerificationContext<? extends Rule> context) {
        MatchRule rule = context.getRule();
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts(rule.getSourcePathPrefix());
        
        String destinationSegmentName = rule.getDestinationSegmentName() != null ? rule.getDestinationSegmentName() : context.getSegmentName();

        Link link = context.getLinkBySegmentNameAndStepName(destinationSegmentName, rule.getDestinationStepName());
        
        if (link != null) {
            Set<Artifact> destinationArtifacts = null;
            if (rule.getDestinationType() == PRODUCTS) {
                destinationArtifacts = new HashSet<>(link.getProducts());
            } else {
                destinationArtifacts = new HashSet<>(link.getMaterials());
            }
            Set<Artifact> srcPrefixedDestinationArtifacts = destinationArtifacts.stream().map(artifact -> prefixSrcDestinationSwap(artifact, rule)).collect(Collectors.toSet());
            if (filteredArtifacts.stream().allMatch(srcPrefixedDestinationArtifacts::contains)) {
                context.consume(filteredArtifacts);
                logResult(log, filteredArtifacts, getRuleType());
                return Boolean.TRUE;
            } else {
                logErrors(log, filteredArtifacts, getRuleType());
                return Boolean.FALSE;
            }
        } else {
            log.warn("no link for destination step {}", rule.getDestinationStepName());
            return Boolean.FALSE;
        }
    }
    
    /*
     * 
     */
    private Artifact prefixSrcDestinationSwap(Artifact destinationArtifact, MatchRule rule) {
        Path path = Paths.get(destinationArtifact.getUri());
        if (StringUtils.hasLength(rule.getDestinationPathPrefix()) && destinationArtifact.getUri().startsWith(rule.getDestinationPathPrefix())) {
            path = Paths.get(rule.getDestinationPathPrefix()).relativize(path);
        }        
        if (StringUtils.hasLength(rule.getSourcePathPrefix())) {
            path = Paths.get(rule.getSourcePathPrefix()).resolve(path);
        }
        return new Artifact(path.toString(), destinationArtifact.getHash());
    }

}
