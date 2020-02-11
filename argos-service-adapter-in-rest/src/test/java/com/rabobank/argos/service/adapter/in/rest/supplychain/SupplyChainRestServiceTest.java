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
package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
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
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRestServiceTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SUPPLY_CHAIN_NAME = "supplyChainName";
    private static final String LABEL_NAME = "labelName";
    @Mock
    private SupplyChainRepository supplyChainRepository;
    @Mock
    private SupplyChainMapper converter;
    @Mock
    private SupplyChain supplyChain;
    @Mock
    private RestSupplyChain restSupplyChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private LabelRepository labelRepository;

    private SupplyChainRestService supplyChainRestService;

    @Mock
    private TreeNode treeNode;

    @BeforeEach
    public void setup() {
        supplyChainRestService = new SupplyChainRestService(supplyChainRepository, hierarchyRepository, converter, labelRepository);
    }

    @Test
    void createSupplyChain_With_UniqueName_Should_Return_201() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestSupplyChainCommand(any())).thenReturn(supplyChain);
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.createSupplyChain(restSupplyChain);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertThat(supplyChainItemResponse.getHeaders().getLocation(), notNullValue());
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
    }

    @Test
    void createSupplyChain_With_Not_Existing_Parent_Label_Should_Return_400() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.createSupplyChain(restSupplyChain));
        assertThat(exception.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label not found : parentLabelId\""));
    }


    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_200() {
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(of(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(any())).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.getSupplyChain(SUPPLY_CHAIN_ID);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
    }

    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_404() {
        when(supplyChainRepository.findBySupplyChainId(any())).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                supplyChainRestService.getSupplyChain("supplyChainName"));
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateSupplyChain() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(converter.convertFromRestSupplyChainCommand(restSupplyChain)).thenReturn(supplyChain);
        when(converter.convertToRestRestSupplyChainItem(supplyChain)).thenReturn(restSupplyChain);
        when(supplyChainRepository.update(SUPPLY_CHAIN_ID, supplyChain)).thenReturn(Optional.of(supplyChain));
        ResponseEntity<RestSupplyChain> response = supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restSupplyChain));
    }

    @Test
    void updateSupplyChainNotExits() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(converter.convertFromRestSupplyChainCommand(restSupplyChain)).thenReturn(supplyChain);
        when(supplyChainRepository.update(SUPPLY_CHAIN_ID, supplyChain)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainId\""));
    }

    @Test
    void updateParentLabelNotExits() {
        when(restSupplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label not found : parentLabelId\""));
    }

    @Test
    void getSupplyChainByPathToRoot() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        when(treeNode.getReferenceId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(supplyChain));
        when(converter.convertToRestRestSupplyChainItem(supplyChain)).thenReturn(restSupplyChain);
        ResponseEntity<RestSupplyChain> response = supplyChainRestService.getSupplyChainByPathToRoot(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME));
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restSupplyChain));
    }

    @Test
    void getSupplyChainByPathToRootNotFound() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.getSupplyChainByPathToRoot(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME)));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainName with path to root labelName\""));
    }

    @Test
    void getSupplyChainByPathToRootSupplyChainNotFound() {
        when(hierarchyRepository.findByNamePathToRootAndType(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        when(treeNode.getReferenceId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.getSupplyChainByPathToRoot(SUPPLY_CHAIN_NAME, List.of(LABEL_NAME)));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"supply chain not found : supplyChainName with path to root labelName\""));
    }
}
