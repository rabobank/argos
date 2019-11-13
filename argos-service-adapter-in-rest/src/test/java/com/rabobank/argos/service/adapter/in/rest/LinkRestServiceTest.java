package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.KeyPairRepository;
import com.rabobank.argos.domain.LinkMetaBlockRepository;
import com.rabobank.argos.domain.SignatureValidator;
import com.rabobank.argos.domain.SupplyChainRepository;
import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Signature;
import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.mapper.LinkMetaBlockMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LinkRestServiceTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainName";
    private static final String HASH = "hash";
    private static final String KEY_ID = "keyId";

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private LinkMetaBlockMapper converter;

    @Mock
    private RestLinkMetaBlock restLinkMetaBlock;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    private LinkRestService restService;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private KeyPairRepository keyPairRepository;

    @Mock
    private Signature signature;

    @Mock
    private KeyPair keyPair;

    @Mock
    private PublicKey publicKey;

    @BeforeEach
    void setUp() {
        restService = new LinkRestService(linkMetaBlockRepository, supplyChainRepository, converter, signatureValidator, keyPairRepository);

    }

    @Test
    void createLinkValidSignature() {

        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));

        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));

        when(signatureValidator.isValid(linkMetaBlock, publicKey)).thenReturn(true);

        when(converter.convertFromRestLinkMetaBlock(restLinkMetaBlock)).thenReturn(linkMetaBlock);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        assertThat(restService.createLink(SUPPLY_CHAIN_ID, restLinkMetaBlock).getStatusCodeValue(), is(204));
        verify(linkMetaBlock).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(linkMetaBlockRepository).save(linkMetaBlock);
    }

    @Test
    void createInValidSignature() {

        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));

        when(signatureValidator.isValid(linkMetaBlock, publicKey)).thenReturn(false);

        when(converter.convertFromRestLinkMetaBlock(restLinkMetaBlock)).thenReturn(linkMetaBlock);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> restService.createLink(SUPPLY_CHAIN_ID, restLinkMetaBlock));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("invalid signature"));
    }

    @Test
    void findLink() {
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        when(linkMetaBlockRepository.findBySupplyChainAndSha(SUPPLY_CHAIN_ID, HASH)).thenReturn(Collections.singletonList(linkMetaBlock));
        when(converter.convertToRestLinkMetaBlock(linkMetaBlock)).thenReturn(restLinkMetaBlock);
        ResponseEntity<List<RestLinkMetaBlock>> response = restService.findLink(SUPPLY_CHAIN_ID, HASH);
        assertThat(response.getBody(), hasSize(1));
        assertThat(response.getBody().get(0), sameInstance(restLinkMetaBlock));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void findLinkUnknownSupplyChain() {
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> restService.findLink(SUPPLY_CHAIN_ID, HASH).getStatusCodeValue());
        assertThat(error.getStatus().value(), is(404));
    }
}