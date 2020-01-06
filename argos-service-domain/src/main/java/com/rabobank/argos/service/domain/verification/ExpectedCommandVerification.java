/*
 * Copyright (C) 2020 Rabobank Nederland
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

import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExpectedCommandVerification implements Verification {
    @Override
    public Priority getPriority() {
        return Priority.EXPECTED_COMMAND;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        List<LinkMetaBlock> failedCommandVerifications = context.getLinkMetaBlocks()
                .stream()
                //check each link for failed required commands
                .filter(linkMetaBlockDoesNotHaveRequiredCommands(context))
                //find the first link metablock that fails
                .collect(Collectors.toList());

        if (!failedCommandVerifications.isEmpty()) {
            log.info("the following links have incorrect commands and will be removed from context: {}",
                    failedCommandVerifications);
            context.removeLinkMetaBlocks(failedCommandVerifications);
        }

        return VerificationRunResult
                .builder()
                .runIsValid(true)
                .build();
    }

    private static Predicate<LinkMetaBlock> linkMetaBlockDoesNotHaveRequiredCommands(VerificationContext context) {
        return linkMetaBlock -> linkCommandsAreNullAndStepCommandsAreNot(context, linkMetaBlock)
                ||
                linkCommandDoNotMatchStepCommands(context, linkMetaBlock);
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
