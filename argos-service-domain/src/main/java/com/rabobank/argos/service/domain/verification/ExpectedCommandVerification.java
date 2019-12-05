package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@Slf4j
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

        failedCommandVerification
                .ifPresent(linkMetaBlock ->
                        log.info("failed verification step:{}, expectedcommands: {} , linkcommands: {}",
                                context.getStepByStepName(linkMetaBlock.getLink().getStepName()).getStepName(),
                                getExpectedCommand(context, linkMetaBlock),
                                linkMetaBlock.getLink().getCommand())
                );

        return VerificationRunResult
                .builder()
                .runIsValid(failedCommandVerification.isEmpty())
                .build();
    }

    private static Predicate<LinkMetaBlock> linkMetaBlockDoesNotHaveRequiredCommands(VerificationContext context) {
        return linkMetaBlock -> bothAreNotNull(context, linkMetaBlock)
                &&
                (linkCommandsAreNullAndStepCommandsAreNot(context, linkMetaBlock)
                        || stepCommandsAreNullAndLinkCommandsAreNot(context, linkMetaBlock)
                        || linkCommandDoNotMatchStepCommands(context, linkMetaBlock)
                );
    }

    private static boolean bothAreNotNull(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return !(linkMetaBlock.getLink().getCommand() == null && getExpectedCommand(context, linkMetaBlock) == null);
    }

    private static boolean linkCommandDoNotMatchStepCommands(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return !getExpectedCommand(context, linkMetaBlock)
                .containsAll(linkMetaBlock.getLink().getCommand());
    }

    private static boolean stepCommandsAreNullAndLinkCommandsAreNot(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return getExpectedCommand(context, linkMetaBlock) == null && linkMetaBlock.getLink().getCommand() != null;
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
