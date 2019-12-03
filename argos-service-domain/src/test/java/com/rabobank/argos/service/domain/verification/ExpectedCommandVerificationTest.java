package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectedCommandVerificationTest {

    public static final String STEP_NAME = "stepName";

    private ExpectedCommandVerification expectedCommandVerification;

    @Mock
    private VerificationContext context;

    @Mock
    private Step step;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LinkMetaBlock linkMetaBlock;

    @BeforeEach
    void setup() {
        expectedCommandVerification = new ExpectedCommandVerification();
    }

    @Test
    void verifyWithCorrectCommandsShouldReturnValid() {
        mockValid();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }


    @Test
    void verifyWithBothNullShouldReturnValid() {
        mockValidBothNull();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithInCorrectCommandsShouldReturnInValid() {
        mockInValid();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(false));
    }

    @Test
    void verifyWithNullCorrectCommandsShouldReturnInValid() {
        mockInValidWithNullInLink();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(false));
    }

    @Test
    void verifyWithNullStepCommandsShouldReturnInValid() {
        mockInValidWithNullInStep();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(false));
    }


    private void mockValidBothNull() {

        when(step.getExpectedCommand()).thenReturn(null);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(null);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
    }

    private void mockInValidWithNullInLink() {
        List<String> stepCommands = asList("command1", "command2");
        when(step.getExpectedCommand()).thenReturn(stepCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(null);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
    }

    private void mockInValidWithNullInStep() {
        List<String> linkCommands = asList("command1", "command2");
        when(step.getExpectedCommand()).thenReturn(null);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(linkCommands);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
    }

    private void mockValid() {
        List<String> commands = asList("command1", "command2");
        when(step.getExpectedCommand()).thenReturn(commands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(commands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));

    }

    private void mockInValid() {
        List<String> stepCommands = asList("command1", "command2");
        List<String> linkCommands = asList("command1", "command3");
        when(step.getExpectedCommand()).thenReturn(stepCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(linkCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));

    }
}