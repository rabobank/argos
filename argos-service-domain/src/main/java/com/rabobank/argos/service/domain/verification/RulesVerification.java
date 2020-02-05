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

import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.rules.RuleVerification;
import com.rabobank.argos.service.domain.verification.rules.RuleVerificationContext;
import com.rabobank.argos.service.domain.verification.rules.RuleVerificationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.RULES;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
@RequiredArgsConstructor
@Slf4j
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
    public VerificationRunResult verify(VerificationContext context) {
        List<VerificationRunResult> verificationRunResults = new ArrayList<>();
        context.layoutSegments().forEach(segment ->
                verificationRunResults.addAll(verifyForSegment(segment, context))
        );

        return verificationRunResults
                .stream()
                .filter(VerificationRunResult::isRunIsValid)
                .findFirst().orElse(VerificationRunResult.valid(false));
    }

    private List<VerificationRunResult> verifyForSegment(LayoutSegment segment, VerificationContext context) {
        return context.getExpectedStepNamesBySegmentName(segment.getName())
                .stream().map(stepName -> verifyStep(context, segment.getName(), stepName))
                .collect(Collectors.toList());
    }

    private VerificationRunResult verifyStep(VerificationContext context, String segmentName, String stepName) {
        log.info("verify rules for step {}", stepName);
        Step step = context.getStepBySegmentNameAndStepName(segmentName, stepName);
        List<LinkMetaBlock> linkMetaBlocks = context.getLinksBySegmentNameAndStepName(segmentName, stepName);

        Map<Boolean, List<LinkMetaBlock>> resultMap = linkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> verifyStep(context, step, linkMetaBlock.getLink()).isRunIsValid()));

        context.removeLinkMetaBlocks(resultMap.getOrDefault(false, emptyList()));
        return VerificationRunResult.valid(true);
    }

    private VerificationRunResult verifyStep(VerificationContext context, Step step, Link link) {
        List<Artifact> materials = new ArrayList<>(link.getMaterials());
        List<Artifact> products = new ArrayList<>(link.getProducts());

        List<RuleVerificationResult> verificationResults = Stream.concat(verifyExpectedProducts(context, step, materials, products),
                verifyExpectedMaterials(context, step, materials)
        ).collect(toList());

        Optional<RuleVerificationResult> optionalNotValidRule = verificationResults.stream()
                .filter(result -> !result.isValid())
                .findFirst();

        return optionalNotValidRule.map(result -> VerificationRunResult
                .valid(result.isValid()))
                .orElseGet(() -> verifyResultAfterAllRulesAreVerified(step,
                        link,
                        collectValidatedArtifacts(verificationResults))
                );
    }


    private Set<Artifact> collectValidatedArtifacts(List<RuleVerificationResult> verificationResults) {
        return verificationResults.stream().map(RuleVerificationResult::getValidatedArtifacts).flatMap(Set::stream).collect(toSet());
    }

    private Stream<RuleVerificationResult> verifyExpectedProducts(VerificationContext verificationContext, Step step, List<Artifact> materials, List<Artifact> products) {
        return step.getExpectedProducts().stream().map(rule -> verifyRule(rule, ruleVerifier -> {
            log.info("verify expected product {} for step {}", rule.getRuleType(), step.getStepName());
            RuleVerificationContext<Rule> context = RuleVerificationContext.builder()
                    .verificationContext(verificationContext)
                    .rule(rule)
                    .materials(materials)
                    .products(products)
                    .build();
            RuleVerificationResult ruleVerificationResult = ruleVerifier.verifyExpectedProducts(context);
            products.removeAll(ruleVerificationResult.getValidatedArtifacts());
            return ruleVerificationResult;
        }));
    }

    private Stream<RuleVerificationResult> verifyExpectedMaterials(VerificationContext verificationContext, Step step, List<Artifact> materials) {
        return step.getExpectedMaterials().stream().map(rule -> verifyRule(rule, ruleVerifier -> {
            log.info("verify expected material {} for step {}", rule.getRuleType(), step.getStepName());
            RuleVerificationContext<Rule> context = RuleVerificationContext.builder()
                    .verificationContext(verificationContext)
                    .rule(rule)
                    .materials(materials)
                    .products(emptyList())
                    .build();
            RuleVerificationResult ruleVerificationResult = ruleVerifier.verifyExpectedMaterials(context);
            materials.removeAll(ruleVerificationResult.getValidatedArtifacts());
            return ruleVerificationResult;
        }));
    }


    private VerificationRunResult verifyResultAfterAllRulesAreVerified(Step step, Link link, Set<Artifact> validatedArtifacts) {
        boolean valid = validatedArtifacts.containsAll(link.getProducts()) && validatedArtifacts.containsAll(link.getMaterials());
        if (!valid) {
            ArrayList<Artifact> unknownArtifacts = new ArrayList<>(link.getProducts());
            unknownArtifacts.addAll(link.getMaterials());
            unknownArtifacts.removeAll(validatedArtifacts);
            log.warn("unknown artifacts in step {}: {}", step.getStepName(), unknownArtifacts);
        }
        return VerificationRunResult.valid(valid);
    }

    private RuleVerificationResult verifyRule(Rule rule, Function<RuleVerification, RuleVerificationResult> ruleVerifyFunction) {
        return Optional.ofNullable(rulesVerificationMap.get(rule.getRuleType()))
                .map(ruleVerifyFunction)
                .map(ruleVerificationResult -> logRuleVerificationResult(rule, ruleVerificationResult))
                .orElseGet(() -> {
                    log.error("rule verification {} not implemented", rule.getRuleType());
                    return RuleVerificationResult.notOkay();
                });
    }

    private RuleVerificationResult logRuleVerificationResult(Rule rule, RuleVerificationResult ruleVerificationResult) {
        log.info("verify result for {} was valid: {}, number of valid artifacts {}",
                rule.getRuleType(),
                ruleVerificationResult.isValid(),
                ruleVerificationResult.getValidatedArtifacts().size());
        return ruleVerificationResult;
    }


}
