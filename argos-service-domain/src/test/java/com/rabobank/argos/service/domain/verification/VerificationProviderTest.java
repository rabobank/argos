/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Artifact artifact;

    @Mock(name = "high")
    private Verification highPrio;

    @Mock(name = "low")
    private Verification lowPrio;

    @Mock
    private VerificationContext verificationContext;

    private List<Verification> verifications;

    @Mock
    private VerificationRunResult verificationRunResultLow;

    @Mock
    private VerificationRunResult verificationRunResultHigh;

    @Captor
    private ArgumentCaptor<VerificationContext> verificationContextArgumentCaptor;

    @Mock
    private VerificationContextsProvider verificationContextsProvider;

    private VerificationProvider verificationProvider;

    @BeforeEach
    void setup() {
        verifications = new ArrayList<>();
        verificationProvider = new VerificationProvider(verifications, verificationContextsProvider);
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        setupMocking();
        when(lowPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultLow);
        when(highPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultHigh);
        when(verificationRunResultLow.isRunIsValid()).thenReturn(true);
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(true);
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(true));
        verify(lowPrio).verify(verificationContextArgumentCaptor.capture());
    }

    @Test
    void verifyShouldProduceFalseVerificationRunResult() {
        setupMocking();
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(false);
        when(highPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultHigh);
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(false));
    }

    private void setupMocking() {
        when(lowPrio.getPriority()).thenReturn(Verification.Priority.EXPECTED_COMMAND);
        when(highPrio.getPriority()).thenReturn(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE);
        when(verificationContextsProvider.createPossibleVerificationContexts(any(), any())).thenReturn(singletonList(verificationContext));
        verifications.add(lowPrio);
        verifications.add(highPrio);
        verificationProvider.init();
    }

}