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
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.rules.RuleVerification;
import com.rabobank.argos.service.domain.verification.rules.RuleVerificationContext;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.RULES;

@Component
@RequiredArgsConstructor
@Slf4j
@ToString
public class RulesVerification implements Verification {

    private final List<RuleVerification> ruleVerificationList;

    private Map<RuleType, RuleVerification> rulesVerificationMap = new EnumMap<>(RuleType.class);

    @Override
    public Priority getPriority() {
        return RULES;
    }

    @PostConstruct
    public void init() {
        ruleVerificationList.forEach(ruleVerification -> rulesVerificationMap.put(ruleVerification.getRuleType(), ruleVerification));
    }

    @Override
    public VerificationRunResult verify(VerificationContext verificationContext) {
        Map<String, Map<Step, Link>> linksBySegmentAndStep = verificationContext.getLinksBySegmentNameAndStep();
        Map<String, Map<String, Link>> linksMap = verificationContext.getLinksBySegmentNameAndStepName();
        
        return linksBySegmentAndStep.keySet().stream()
                .map(segmentName -> verifyForSegment(
                        linksMap, 
                        segmentName,
                        linksBySegmentAndStep.get(segmentName)))
                .filter(result1 -> !result1)
                .findFirst()
                .map(result2 -> VerificationRunResult.builder().runIsValid(false).build())
                .orElse(VerificationRunResult.builder().runIsValid(true).build());
    }

    private boolean verifyForSegment(Map<String, Map<String, Link>> linksMap, String segmentName, Map<Step, Link> stepMap) {
        return stepMap.keySet().stream().map(step -> verifyStep(linksMap, segmentName, step, stepMap.get(step))).noneMatch(result -> !result);
    }

    private boolean verifyStep(Map<String, Map<String, Link>> linksMap, String segmentName, Step step, Link link) {
        if (link == null) {
            log.warn("no links for step [{}]", step.getName());
            return false;
        }
        return verifyLink(linksMap, segmentName, step, link);
    }

    private boolean verifyLink(Map<String, Map<String, Link>> linksMap, String segmentName, Step step, Link link) {
        return  verifyArtifactsByType(linksMap, segmentName, step, new HashSet<>(link.getMaterials()), link, ArtifactType.MATERIALS)
                && verifyArtifactsByType(linksMap, segmentName, step, new HashSet<>(link.getProducts()), link, ArtifactType.PRODUCTS);
    }

    private boolean verifyArtifactsByType(Map<String, Map<String, Link>> linksMap, String segmentName, Step step,
            Set<Artifact> artifacts, Link link, ArtifactType type) {
        ArtifactsVerificationContext artifactsContext = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .link(link)
                .notConsumedArtifacts(artifacts)
                .linksMap(linksMap)
                .build();

        return getExpectedArtifactRulesByType(step, type).stream()
                .map(rule -> verifyRule(rule, ruleVerifier -> {
                    log.info("verify expected [{}] [{}] for step [{}]", type, rule.getRuleType(), step.getName());
                    RuleVerificationContext<Rule> context = RuleVerificationContext.builder()
                            .rule(rule)
                            .artifactsContext(artifactsContext)
                            .build();
                    return ruleVerifier.verify(context);
                }))
                .filter(valid -> !valid)
                .findFirst()
                .orElseGet(() -> validateNotConsumedArtifacts(artifactsContext));        
    }

    private boolean verifyRule(Rule rule, Predicate<RuleVerification> ruleVerifyFunction) {
        return Optional.ofNullable(rulesVerificationMap.get(rule.getRuleType()))
                .map(ruleVerifyFunction::test)
                .orElseGet(() -> {
                    log.error("rule verification [{}] not implemented", rule.getRuleType());
                    return false;
                });
    }
    
    private List<Rule> getExpectedArtifactRulesByType(Step step, ArtifactType type){
        if(type == ArtifactType.PRODUCTS) {
            if (step.getExpectedProducts() != null) {
                return step.getExpectedProducts();
            } else {
                return List.of();
            }
        }else {
            if (step.getExpectedMaterials() != null) {
                return step.getExpectedMaterials();
            } else {
                return List.of();
            }
        }
    }
    
    private boolean validateNotConsumedArtifacts(ArtifactsVerificationContext artifactsContext) {
        if (!artifactsContext.getNotConsumedArtifacts().isEmpty()) {
            artifactsContext.getNotConsumedArtifacts().stream().forEach(artifact -> {
                log.info("Not consumed artifact [{}]",
                        artifact);
            });
            return false;
        }
        return true;
    }

}
