package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.Optional;
import java.util.function.Predicate;

public class ExpectedCommandVerification implements Verification {
    @Override
    public Priority getPriority() {
        return Priority.EXPECTED_COMMAND;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        // the optional is filled with the first failed command verification or empty
        Optional<LinkMetaBlock> failedCommandVerification = context.getLinkMetaBlocks()
                .stream()
                //check each link for failed required commands
                .filter(linkMetaBlockDoesNotHaveRequiredCommands(context))
                //find the first linkmetablock that fails
                .findFirst();
        return VerificationRunResult
                .builder()
                .runIsValid(failedCommandVerification.isEmpty())
                .build();
    }

    private static Predicate<LinkMetaBlock> linkMetaBlockDoesNotHaveRequiredCommands(VerificationContext context) {
        return linkMetaBlock -> !context
                .getStepByStepName(linkMetaBlock.getLink().getStepName())
                .getExpectedCommand()
                .containsAll(linkMetaBlock.getLink().getCommand());
    }

}
