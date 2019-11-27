package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    private VerificationProvider verificationProvider;
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;


    @BeforeEach
    public void setup() {

    }

    @Test
    void verifyShouldProduceVerificationRunResult() {

    }

}