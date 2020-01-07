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
package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationRestServiceTest {

    @Mock
    private VerificationProvider verificationProvider;

    @Mock
    private LayoutMetaBlockRepository repository;

    @Mock
    private ArtifactMapper artifactMapper;

    @Mock
    private VerificationResultMapper verificationResultMapper;

    @Mock
    private RestVerifyCommand restVerifyCommand;

    @Mock
    private Artifact artifact;

    @Mock
    private RestArtifact restArtifact;

    @Mock
    private LayoutMetaBlock layoutMetaBlockMetaBlock;

    private VerificationRestService verificationRestService;


    @BeforeEach
    void setup() {
        verificationRestService = new VerificationRestService(
                verificationProvider,
                repository,
                artifactMapper,
                verificationResultMapper);

    }

    @Test
    void performVerificationShouldReturnOk() {
        VerificationRunResult runResult = VerificationRunResult.okay();
        RestVerificationResult restVerificationResult = new RestVerificationResult();
        restVerificationResult.setRunIsValid(true);
        when(repository.findBySupplyChainId(eq("supplyChainId")))
                .thenReturn(singletonList(layoutMetaBlockMetaBlock));
        when(restVerifyCommand.getExpectedProducts()).thenReturn(singletonList(restArtifact));
        when(artifactMapper.mapToArtifacts(any())).thenReturn(singletonList(artifact));
        when(verificationProvider.verifyRun(any(), any())).thenReturn(runResult);
        when(verificationResultMapper.mapToRestVerificationResult(eq(runResult))).thenReturn(restVerificationResult);
        ResponseEntity<RestVerificationResult> result = verificationRestService.performVerification("supplyChainId", restVerifyCommand);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getRunIsValid(), is(true));
    }

    @Test
    void performVerificationWithNoLayoutShouldReturnError() {
        when(repository.findBySupplyChainId(eq("supplyChainId")))
                .thenReturn(emptyList());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> verificationRestService.performVerification("supplyChainId", restVerifyCommand));
        assertThat(error.getStatus().value(), is(400));
    }
}