package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextTest {
    public static final String STEP_NAME = "stepName";
    private VerificationContext verificationContext;
    private List<LinkMetaBlock> linkMetaBlocks;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LayoutMetaBlock layoutMetaBlock;

    @BeforeEach
    void setup() {
        when(layoutMetaBlock.getLayout().getSteps())
                .thenReturn(singletonList(Step.builder().stepName(STEP_NAME).build()));

        linkMetaBlocks = singletonList(LinkMetaBlock
                .builder().link(Link.builder().stepName(STEP_NAME).build()).build());

        verificationContext = VerificationContext
                .builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(linkMetaBlocks)
                .build();
    }

    @Test
    void getStepByStepNameWithValidStepReturnsResult() {
        Step step = verificationContext.getStepByStepName(STEP_NAME);
        assertThat(step.getStepName(), is(STEP_NAME));
    }

    @Test
    void getLinksByStepNameWithInValidStepReturnsException() {
        VerificationError error = assertThrows(VerificationError.class, () -> verificationContext.getStepByStepName("incorrect"));
        assertThat(error.getMessage(), Is.is("step with name: incorrect could not be found"));
    }

    @Test
    void getStepByStepNameWithInValidStepReturnsException() {
        VerificationError error = assertThrows(VerificationError.class, () -> verificationContext.getLinksByStepName("incorrect"));
        assertThat(error.getMessage(), Is.is("LinkMetaBlocks with step name: incorrect could not be found"));
    }
}