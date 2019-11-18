package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.service.adapter.in.rest.mapper.SupplyChainMapper;
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

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRestServiceTest {

    private static final String NAME = "name";
    @Mock
    private SupplyChainRepository supplyChainRepository;
    @Mock
    private SupplyChainMapper converter;
    @Mock
    private SupplyChain supplyChain;
    @Mock
    private RestSupplyChainItem supplyChainItem;
    @Mock
    private RestCreateSupplyChainCommand createSupplyChainCommand;
    @Mock
    private HttpServletRequest httpServletRequest;

    private SupplyChainRestService supplyChainRestService;

    @BeforeEach
    public void setup() {
        supplyChainRestService = new SupplyChainRestService(supplyChainRepository, converter);
    }

    @Test
    void createSupplyChain_With_UniqueName_Should_Return_201() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(createSupplyChainCommand.getName()).thenReturn(NAME);
        when(supplyChainRepository.findByName(NAME)).thenReturn(Collections.emptyList());
        when(converter.convertFromRestSupplyChainCommand(any(), any())).thenReturn(supplyChain);
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(supplyChainItem);
        ResponseEntity<RestSupplyChainItem> supplyChainItemResponse = supplyChainRestService.createSupplyChain(createSupplyChainCommand);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertThat(supplyChainItemResponse.getHeaders().getLocation(), notNullValue());
        assertThat(supplyChainItemResponse.getBody(), is(supplyChainItem));
    }

    @Test
    void createSupplyChain_With_NonUniqueName_Should_Return_400() {
        when(createSupplyChainCommand.getName()).thenReturn(NAME);
        when(supplyChainRepository.findByName(NAME)).thenReturn(Collections.singletonList(supplyChain));
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            supplyChainRestService.createSupplyChain(createSupplyChainCommand);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_200() {
        when(supplyChainRepository.findBySupplyChainId(any())).thenReturn(of(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(supplyChainItem);
        ResponseEntity<RestSupplyChainItem> supplyChainItemResponse = supplyChainRestService.getSupplyChain("supplyChainName");
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(supplyChainItemResponse.getBody(), is(supplyChainItem));
    }

    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_404() {
        when(supplyChainRepository.findBySupplyChainId(any())).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            supplyChainRestService.getSupplyChain("supplyChainName");
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void searchSupplyChains_With_Name_Should_Return_200() {
        when(supplyChainRepository.findByName(any())).thenReturn(Collections.singletonList(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(supplyChainItem);
        ResponseEntity<List<RestSupplyChainItem>> responseEntity = supplyChainRestService.searchSupplyChains(NAME);
        assertThat(responseEntity.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(responseEntity.getBody(), is(instanceOf(List.class)));
    }

    @Test
    void searchSupplyChains_Without_Name_Should_Return_200() {
        when(supplyChainRepository.findAll()).thenReturn(Collections.singletonList(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(supplyChainItem);
        ResponseEntity<List<RestSupplyChainItem>> responseEntity = supplyChainRestService.searchSupplyChains(null);
        assertThat(responseEntity.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(responseEntity.getBody(), is(instanceOf(List.class)));
    }

}