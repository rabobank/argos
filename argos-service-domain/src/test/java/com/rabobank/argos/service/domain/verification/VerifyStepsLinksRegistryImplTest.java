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

@ExtendWith(MockitoExtension.class)
class VerifyStepsLinksRegistryImplTest {

    private VerificationContext verificationContext;

    @Mock
    private Map<String, List<LinkMetaBlock>> linksByStepName;

    @Mock
    private Map<String, List<Step>> stepsByStepName;

    @BeforeEach
    void setup() {
    }

    @Test
    void getStepByStepName() {
    }

    @Test
    void getLinksByStepName() {
    }
}