package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequiredNumberOfLinksVerificationTest {

    public static final String STEP_NAME = "stepName";
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VerificationContext context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Step step;

    private LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder().build();

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();
    }

    @Test
    void getPriority() {
        assertThat(requiredNumberOfLinksVerification.getPriority(), is(Verification.Priority.REQUIRED_NUMBER_OF_LINKS));
    }

    @Test
    void verifyWithRequiredNumberOfLinksShouldReturnValid() {
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(singletonList(step));
        when(step.getStepName()).thenReturn(STEP_NAME);
        when(step.getRequiredNumberOfLinks()).thenReturn(1);
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(singletonList(linkMetaBlock));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithNoRequiredNumberOfLinksShouldReturnInValid() {
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(singletonList(step));
        when(step.getStepName()).thenReturn(STEP_NAME);
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(singletonList(linkMetaBlock));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}