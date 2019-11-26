package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.verification.VerificationProvider.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    VerificationProvider verificationProvider;

    @Mock
    LayoutMetaBlock layoutMetaBlock;
    List<Artifact> expectedProducts = Collections.emptyList();

    @BeforeEach
    public void setup() {
        verificationProvider = new VerificationProvider();
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        VerificationRunResult verificationRunResult = verificationProvider.verifyRun(layoutMetaBlock, expectedProducts);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }
}