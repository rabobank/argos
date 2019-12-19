/*
 * Copyright (C) 2019 Rabobank Nederland
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

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    private static final String RUN_ID = "runId";
    private static final String SUPPLYCHAIN_ID = "supplyChainId";
    private static final String STEPNAME = "stepName";
    private VerificationProvider verificationProvider;

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private RunIdResolver runIdResolver;

    private List<Verification> verifications;

    @Mock(name = "high")
    private Verification highPrio;

    @Mock(name = "low")
    private Verification lowPrio;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Artifact artifact;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    private Step step;

    @Mock
    private Link link;

    @Mock
    private VerificationRunResult verificationRunResultLow;

    @Mock
    private VerificationRunResult verificationRunResultHigh;

    @Captor
    private ArgumentCaptor<VerificationContext> verificationContextArgumentCaptor;

    @Mock
    private LayoutSegment segment;

    @BeforeEach
    public void setup() {
        verifications = new ArrayList<>();
        verificationProvider = new VerificationProvider(linkMetaBlockRepository, runIdResolver, verifications);
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        setupMocking();

        when(verificationRunResultLow.isRunIsValid()).thenReturn(true);
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(true);


        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(true));

        verify(lowPrio).verify(verificationContextArgumentCaptor.capture());
        VerificationContext verificationContext = verificationContextArgumentCaptor.getValue();
        assertThat(verificationContext.getLayoutMetaBlock(), sameInstance(layoutMetaBlock));
        assertThat(verificationContext.getLinkMetaBlocks(), hasItem(linkMetaBlock));
        assertThat(verificationContext.getSegment(), sameInstance(segment));

        verify(highPrio).verify(any(VerificationContext.class));

    }

    @Test
    void verifyShouldProduceFalseVerificationRunResult() {
        setupMocking();
        when(verificationRunResultLow.isRunIsValid()).thenReturn(false);
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(true);
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(false));

    }

    private void setupMocking() {
        when(lowPrio.getPriority()).thenReturn(Verification.Priority.BUILDSTEPS_COMPLETED);
        when(highPrio.getPriority()).thenReturn(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE);
        verifications.add(lowPrio);
        verifications.add(highPrio);
        verificationProvider.init();

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLYCHAIN_ID);


        when(linkMetaBlock.getLink()).thenReturn(link);
        when(link.getStepName()).thenReturn(STEPNAME);

        when(runIdResolver.getRunIdPerSegment(layoutMetaBlock, List.of(artifact)))
                .thenReturn(List.of(RunIdsWithSegment.builder().segment(segment).runIds(Set.of(RUN_ID)).build()));
        when(linkMetaBlockRepository.findByRunId(SUPPLYCHAIN_ID, RUN_ID)).thenReturn(List.of(linkMetaBlock));

        when(lowPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultLow);
        when(highPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultHigh);
    }

}
