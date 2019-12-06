/*
 * Copyright (C) 2019 Rabobank Nederland
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
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.rabobank.argos.domain.layout.DestinationType.MATERIALS;
import static com.rabobank.argos.domain.layout.DestinationType.PRODUCTS;
import static com.rabobank.argos.service.domain.verification.rules.RuleVerificationContext.filterArtifacts;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Component
public class MatchRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MATCH;
    }

    @Override
    public RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        return verify(context, filterSourceArtifacts(context.getLink().getProducts(), context.getRule()));
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        return verify(context, filterSourceArtifacts(context.getLink().getMaterials(), context.getRule()));
    }

    private RuleVerificationResult verify(RuleVerificationContext<? extends Rule> context, Stream<Artifact> filteredSourceArtifacts) {
        MatchRule rule = context.getRule();
        List<LinkMetaBlock> linksByStepName = context.getVerificationContext().getLinksByStepName(rule.getDestinationStepName());
        if (!linksByStepName.isEmpty()) {
            if (rule.getDestinationType() == PRODUCTS) {
                return checkResult(getLinkStream(linksByStepName).map(destinationLink ->
                        verifyArtifacts(filteredSourceArtifacts, filterDestinationArtifacts(destinationLink.getProducts(), rule))));
            } else if (rule.getDestinationType() == MATERIALS) {
                return checkResult(getLinkStream(linksByStepName).map(destinationLink ->
                        verifyArtifacts(filteredSourceArtifacts, filterDestinationArtifacts(destinationLink.getMaterials(), rule))));
            } else {
                log.error("unknown destination type {}", rule.getDestinationType());
                return RuleVerificationResult.notOkay();
            }
        } else {
            log.warn("no links for destination step {}", rule.getDestinationStepName());
            return RuleVerificationResult.notOkay();
        }
    }

    private Stream<Link> getLinkStream(List<LinkMetaBlock> linksByStepName) {
        return linksByStepName.stream().map(LinkMetaBlock::getLink);
    }

    private RuleVerificationResult checkResult(Stream<RuleVerificationResult> resultStream) {
        return resultStream.filter(RuleVerificationResult::isValid).findFirst().orElse(RuleVerificationResult.notOkay());
    }

    private Stream<Artifact> filterDestinationArtifacts(List<Artifact> destinationArtifacts, MatchRule matchRule) {
        return filterArtifacts(destinationArtifacts, matchRule.getPattern(), matchRule.getDestinationPathPrefix());
    }

    private Stream<Artifact> filterSourceArtifacts(List<Artifact> sourceArtifacts, MatchRule matchRule) {
        return filterArtifacts(sourceArtifacts, matchRule.getPattern(), matchRule.getSourcePathPrefix());
    }

    private RuleVerificationResult verifyArtifacts(Stream<Artifact> filteredSourceArtifacts, Stream<Artifact> filteredDestinationArtifacts) {
        Set<Artifact> artifacts = filteredSourceArtifacts.collect(toSet());
        if (areEqual(artifacts.stream().map(Artifact::getHash).collect(toSet()), filteredDestinationArtifacts.map(Artifact::getHash).collect(toSet()))) {
            return RuleVerificationResult.okay(artifacts);
        } else {
            return RuleVerificationResult.notOkay();
        }
    }

    private boolean areEqual(Set<String> filteredSourceHashes, Set<String> filteredDestinationHashes) {
        return filteredSourceHashes.size() == filteredDestinationHashes.size() && filteredSourceHashes.containsAll(filteredDestinationHashes);
    }


}
