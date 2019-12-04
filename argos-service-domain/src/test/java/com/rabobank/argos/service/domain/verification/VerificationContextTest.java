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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
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
                .thenReturn(List.of(Step.builder().stepName(STEP_NAME).build()));

        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder().stepName(STEP_NAME).build()).build()));

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
        assertThat(verificationContext.getLinksByStepName("incorrect"), empty());
    }

    @Test
    void getLinksByStepName() {
        assertThat(verificationContext.getLinksByStepName(STEP_NAME).get(0), sameInstance(linkMetaBlocks.get(0)));
    }

    @Test
    void getStepByStepName() {
        assertThat(verificationContext.getStepByStepName(STEP_NAME), sameInstance(layoutMetaBlock.getLayout().getSteps().get(0)));
    }

    @Test
    void removeLinkMetaBlocks() {
        verificationContext.removeLinkMetaBlocks(List.of(linkMetaBlocks.get(0)));
        assertThat(verificationContext.getLinkMetaBlocks(), empty());
        assertThat(verificationContext.getLinksByStepName(STEP_NAME), empty());
    }
}