package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.domain.repository.LayoutMetaBlockRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockRepository repository;

    @Mock
    private RestLayoutMetaBlock restLayoutMetaBlock;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutRestService service;

    @Mock
    private LayoutValidatorService validator;

    @BeforeEach
    void setUp() {
        service = new LayoutRestService(converter, repository, validator);
    }

    @Test
    void createLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);

        when(layoutMetaBlock.getLayoutMetaBlockId()).thenReturn(METABLOCK_ID);


        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);

        assertThat(responseEntity.getStatusCodeValue(), is(201));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
        assertThat(responseEntity.getHeaders().getLocation().getPath(), is("/" + METABLOCK_ID));

        verify(repository).save(layoutMetaBlock);
        verify(validator).validate(layoutMetaBlock);

    }


    @Test
    void getLayout() {
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(repository.findBySupplyChainAndId(SUPPLY_CHAIN_ID, METABLOCK_ID)).thenReturn(Optional.of(layoutMetaBlock));
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.getLayout(SUPPLY_CHAIN_ID, METABLOCK_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
    }

    @Test
    void getLayoutNotFound() {
        when(repository.findBySupplyChainAndId(SUPPLY_CHAIN_ID, METABLOCK_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.getLayout(SUPPLY_CHAIN_ID, METABLOCK_ID);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }

    @Test
    void findLayout() {
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(repository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Collections.singletonList(layoutMetaBlock));
        ResponseEntity<List<RestLayoutMetaBlock>> responseEntity = service.findLayout(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), contains(restLayoutMetaBlock));
    }

    @Test
    void updateLayout() {

        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(repository.update(SUPPLY_CHAIN_ID, METABLOCK_ID, layoutMetaBlock)).thenReturn(true);
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.updateLayout(SUPPLY_CHAIN_ID, METABLOCK_ID, restLayoutMetaBlock);

        verify(layoutMetaBlock).setLayoutMetaBlockId(METABLOCK_ID);
        verify(layoutMetaBlock).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(validator).validate(layoutMetaBlock);

        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
    }

    @Test
    void updateLayoutNotFound() {
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(repository.update(SUPPLY_CHAIN_ID, METABLOCK_ID, layoutMetaBlock)).thenReturn(false);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.updateLayout(SUPPLY_CHAIN_ID, METABLOCK_ID, restLayoutMetaBlock);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }
}