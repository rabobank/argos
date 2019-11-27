package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
@Slf4j
public class CompleteBuildRunVerification implements Verification {
    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verifySteps(context.getLinkMetaBlocks(), context.getLayoutMetaBlock());
    }

    private VerificationRunResult verifySteps(List<LinkMetaBlock> linkMetaBlocks, LayoutMetaBlock layoutMetaBlock) {
        List<String> linkBuildSteps = linkMetaBlocks.stream().map(LinkMetaBlock::getLink).map(Link::getStepName).collect(toList());
        Set<String> expectedSteps = layoutMetaBlock.getLayout().getSteps().stream().map(Step::getStepName).collect(toSet());
        log.info("linkBuildSteps: {} , expectedSteps: {}", linkBuildSteps, expectedSteps);

        return VerificationRunResult.builder().runIsValid(
                linkBuildSteps.size() == expectedSteps.size() && expectedSteps.containsAll(linkBuildSteps))
                .build();
    }
}
