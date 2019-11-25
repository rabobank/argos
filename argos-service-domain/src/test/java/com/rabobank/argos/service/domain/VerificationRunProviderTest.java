package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.service.domain.VerificationRunProvider.VerificationRunResult;
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
class VerificationRunProviderTest {

    VerificationRunProvider verificationRunProvider;

    @Mock
    LayoutMetaBlock layoutMetaBlock;
    List<Artifact> expectedProducts = Collections.emptyList();

    @BeforeEach
    public void setup() {
        verificationRunProvider = new VerificationRunProvider();
    }

    @Test
    void verifyRunShouldProduceVerificationRunResult() {
        VerificationRunResult verificationRunResult = verificationRunProvider.verifyRun(layoutMetaBlock, expectedProducts);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }
}