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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutValidatorServiceTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String KEY_ID_1 = "keyId1";
    private static final String KEY_ID_2 = "keyId2";

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private SignatureValidatorService signatureValidatorService;

    @Mock
    private KeyPairRepository keyPairRepository;

    private LayoutValidatorService service;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Signature signature;

    @Mock
    private Layout layout;

    @Mock
    private Step step;

    @Mock
    private LayoutSegment layoutSegment;

    @BeforeEach
    void setUp() {
        service = new LayoutValidatorService(supplyChainRepository, signatureValidatorService, keyPairRepository);
    }

    @Test
    void validateAllOkay() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(layoutMetaBlock.getSignatures()).thenReturn(singletonList(signature));
        when(layoutMetaBlock.getLayout()).thenReturn(layout);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_1));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_2));

        when(keyPairRepository.exists(KEY_ID_1)).thenReturn(true);
        when(keyPairRepository.exists(KEY_ID_2)).thenReturn(true);

        service.validate(layoutMetaBlock);
        verify(signatureValidatorService).validateSignature(layout, signature);
    }

    @Test
    void validateDuplicateKeyId() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(signature.getKeyId()).thenReturn(KEY_ID_1);
        when(layoutMetaBlock.getSignatures()).thenReturn(Arrays.asList(signature, signature));
        when(layoutMetaBlock.getLayout()).thenReturn(layout);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_1));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_2));

        when(keyPairRepository.exists(KEY_ID_1)).thenReturn(true);
        when(keyPairRepository.exists(KEY_ID_2)).thenReturn(true);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseStatusException.getReason(), is("layout can't be signed more than one time with the same keyId"));
    }

    @Test
    void validateKey2NotFound() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_1));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_2));

        when(keyPairRepository.exists(KEY_ID_1)).thenReturn(true);
        when(keyPairRepository.exists(KEY_ID_2)).thenReturn(false);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseStatusException.getReason(), is("keyId keyId2 not found"));
    }

    @Test
    void validateKey1NotFound() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_1));
        when(keyPairRepository.exists(KEY_ID_1)).thenReturn(false);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseStatusException.getReason(), is("keyId keyId1 not found"));
    }

    @Test
    void validateNoSupplyChain() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseStatusException.getReason(), is("supply chain not found : " + SUPPLY_CHAIN_ID));
    }
}
