package com.rabobank.argos.service.adapter.in.rest.link;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

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

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private LinkMetaBlockMapper converter;

    @Mock
    private SignatureValidatorService signatureValidatorService;

    @Mock
    private RestLinkMetaBlock restLinkMetaBlock;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    private LinkRestService restService;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private Signature signature;

    @Mock
    private Link link;

    @BeforeEach
    void setUp() {
        restService = new LinkRestService(linkMetaBlockRepository, supplyChainRepository, converter, signatureValidatorService);

    }

    @Test
    void createLinkValidSignature() {

        when(linkMetaBlock.getLink()).thenReturn(link);
        when(linkMetaBlock.getSignature()).thenReturn(signature);

        when(converter.convertFromRestLinkMetaBlock(restLinkMetaBlock)).thenReturn(linkMetaBlock);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        assertThat(restService.createLink(SUPPLY_CHAIN_ID, restLinkMetaBlock).getStatusCodeValue(), is(204));
        verify(linkMetaBlock).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(linkMetaBlockRepository).save(linkMetaBlock);
        verify(signatureValidatorService).validateSignature(link, signature);
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
