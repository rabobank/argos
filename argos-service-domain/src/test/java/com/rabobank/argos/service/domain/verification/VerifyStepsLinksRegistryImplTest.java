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

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VerifyStepsLinksRegistryImplTest {

    private VerifyStepsLinksRegistry verifyStepsLinksRegistry;

    @Mock
    private Map<String, List<LinkMetaBlock>> linksByStepName;

    @Mock
    private Map<String, List<Step>> stepsByStepName;

    @BeforeEach
    void setup() {
        verifyStepsLinksRegistry = VerifyStepsLinksRegistryImpl.builder().build();
    }

    @Test
    void getStepByStepName() {
        assertThrows(VerificationError.class, () -> verifyStepsLinksRegistry.getStepByStepName("stepName"));
        ;
    }

    @Test
    void getLinksByStepName() {
        assertThrows(VerificationError.class, () -> verifyStepsLinksRegistry.getLinksByStepName("stepName"));
    }
}