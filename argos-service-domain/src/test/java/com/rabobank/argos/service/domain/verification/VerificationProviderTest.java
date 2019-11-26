package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    private VerificationProvider verificationProvider;
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;
    private List<Artifact> expectedProducts = new ArrayList<>();

    @BeforeEach
    public void setup() {
        verificationProvider = new VerificationProvider(linkMetaBlockRepository);
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        VerificationRunResult verificationRunResult = verificationProvider.verifyRun(layoutMetaBlock, expectedProducts);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }

}