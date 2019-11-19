package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.domain.repository.LayoutMetaBlockRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutRestServiceTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String METABLOCK_ID = "metaBlockId";
    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockRepository repository;

    @Mock
    private RestLayoutMetaBlock restLayoutMetaBlock;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutRestService service;

    @BeforeEach
    void setUp() {
        service = new LayoutRestService(supplyChainRepository, converter, repository);
    }

    @Test
    void createLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);

        when(layoutMetaBlock.getLayoutMetaBlockId()).thenReturn(METABLOCK_ID);

        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);

        assertThat(responseEntity.getStatusCodeValue(), is(201));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
        assertThat(responseEntity.getHeaders().getLocation().getPath(), is("/" + METABLOCK_ID));

        verify(repository).save(layoutMetaBlock);

    }

    @Test
    void createLayoutNoSupplyChain() {
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.createLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseStatusException.getReason(), is("supply chain not found : " + SUPPLY_CHAIN_ID));
    }

    @Test
    void getLayout() {
    }

    @Test
    void findLayout() {
    }
}