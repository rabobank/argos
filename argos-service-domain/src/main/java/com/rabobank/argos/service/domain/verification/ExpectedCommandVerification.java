package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
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
        return linkMetaBlock -> linkCommandsAreNullAndStepCommandsAreNot(context, linkMetaBlock) || linkCommandDoNotMatchStepCommands(context, linkMetaBlock);
    }

    private static boolean linkCommandDoNotMatchStepCommands(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return !getExpectedCommand(context, linkMetaBlock)
                .containsAll(linkMetaBlock.getLink().getCommand());
    }

    private static boolean linkCommandsAreNullAndStepCommandsAreNot(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return linkMetaBlock.getLink().getCommand() == null && getExpectedCommand(context, linkMetaBlock) != null;
    }

    private static List<String> getExpectedCommand(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return context
                .getStepByStepName(linkMetaBlock.getLink().getStepName())
                .getExpectedCommand();
    }

}
