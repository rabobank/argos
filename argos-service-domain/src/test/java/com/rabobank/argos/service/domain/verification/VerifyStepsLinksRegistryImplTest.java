package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(MockitoExtension.class)
class VerifyStepsLinksRegistryImplTest {

    private VerifyStepsLinksRegistry verifyStepsLinksRegistry;

    @Mock
    private Map<String, List<LinkMetaBlock>> linksByStepName;

    @Mock
    private Map<String, List<Step>> stepsByStepName;

    @BeforeEach
    void setup() {
        verifyStepsLinksRegistry = new VerifyStepsLinksRegistryImpl(linksByStepName, stepsByStepName);
    }

    @Test
    void getStepByStepName() {
        assertThat(verifyStepsLinksRegistry.getLinksByStepName(), hasSize(0));
    }

    @Test
    void getLinksByStepName() {
        assertThat(verifyStepsLinksRegistry.getStepByStepName(), is(nullValue()));
    }
}