package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Step;
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
class VerifyRunStepsLinksRegistryImplTest {

    private VerifyRunStepsLinksRegistry verifyRunStepsLinksRegistry;

    @Mock
    private Map<String, List<LinkMetaBlock>> linksByStepName;

    @Mock
    private Map<String, List<Step>> stepsByStepName;

    @BeforeEach
    void setup() {
        verifyRunStepsLinksRegistry = new VerifyRunStepsLinksRegistryImpl(linksByStepName, stepsByStepName);
    }

    @Test
    void getStepByStepName() {
        assertThat(verifyRunStepsLinksRegistry.getLinksByStepName(), hasSize(0));

    }

    @Test
    void getLinksByStepName() {
        assertThat(verifyRunStepsLinksRegistry.getStepByStepName(), is(nullValue()));
    }
}