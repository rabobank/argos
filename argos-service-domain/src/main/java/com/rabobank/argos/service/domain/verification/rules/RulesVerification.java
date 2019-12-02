package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.Verification;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.RULES;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
@Slf4j
public class RulesVerification implements Verification {

    private final List<RuleVerification> ruleVerificationList;

    private Map<Class<? extends Rule>, RuleVerification> rulesVerificationMap = new HashMap<>();

    @Override
    public Priority getPriority() {
        return RULES;
    }

    @PostConstruct
    public void init() {
        ruleVerificationList.forEach(ruleVerification -> rulesVerificationMap.put(ruleVerification.getRuleClass(), ruleVerification));
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        List<String> stepNames = context.getExpectedStepNames();

        return stepNames.stream().map(stepName -> verifyStep(context, stepName))
                .filter(result -> !result.isRunIsValid())
                .findFirst().orElse(VerificationRunResult.okay());
    }

    private VerificationRunResult verifyStep(VerificationContext context, String stepName) {
        log.info("verify {}", stepName);
        Step step = context.getStepByStepName(stepName);
        List<LinkMetaBlock> linkMetaBlocks = context.getLinksByStepName(stepName);

        Map<Boolean, List<LinkMetaBlock>> resultMap = linkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> verifyStep(step, linkMetaBlock.getLink()).isRunIsValid()));

        context.removeLinkMetaBlocks(resultMap.getOrDefault(false, emptyList()));
        return VerificationRunResult.valid(!resultMap.getOrDefault(true, emptyList()).isEmpty());
    }

    private VerificationRunResult verifyStep(Step step, Link link) {
        Set<Artifact> validatedArtifacts = new HashSet<>();
        return step.getExpectedProducts().stream()
                .map(rule -> verifyRule(rule, link, validatedArtifacts))
                .filter(result -> !result.isRunIsValid())
                .findFirst()
                .orElseGet(() -> {
                    boolean valid = validatedArtifacts.containsAll(link.getProducts());
                    if (!valid) {
                        ArrayList<Artifact> unknownArtifacts = new ArrayList<>(link.getProducts());
                        unknownArtifacts.removeAll(validatedArtifacts);
                        log.warn("unknown artifacts in step {}\n{}", step.getStepName(), unknownArtifacts);
                    }
                    return VerificationRunResult.valid(valid);
                });
    }

    private VerificationRunResult verifyRule(Rule rule, Link link, Set<Artifact> validatedArtifacts) {
        RuleVerificationContext<? extends Rule> context = RuleVerificationContext.builder()
                .rule(rule)
                .link(link)
                .validatedArtifacts(validatedArtifacts).build();
        RuleVerification ruleVerification = rulesVerificationMap.get(rule.getClass());
        if (ruleVerification != null) {
            log.info("verify rule {} on step {}", rule.getClass().getSimpleName(), link.getStepName());
            return ruleVerification.verifyExpectedProducts(context);
        } else {
            log.error("{} not implemented", rule.getClass());
            return VerificationRunResult.notOkay();
        }
    }


}
