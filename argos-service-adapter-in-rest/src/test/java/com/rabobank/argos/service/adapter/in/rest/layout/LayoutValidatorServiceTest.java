package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.model.Signature;
import com.rabobank.argos.domain.model.Step;
import com.rabobank.argos.domain.repository.KeyPairRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
        when(layout.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_2));

        when(keyPairRepository.exists(KEY_ID_1)).thenReturn(true);
        when(keyPairRepository.exists(KEY_ID_2)).thenReturn(true);

        service.validate(layoutMetaBlock);
        verify(signatureValidatorService).validateSignature(layout, signature);
    }

    @Test
    void validateKey2NotFound() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_ID_1));
        when(layout.getSteps()).thenReturn(singletonList(step));
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