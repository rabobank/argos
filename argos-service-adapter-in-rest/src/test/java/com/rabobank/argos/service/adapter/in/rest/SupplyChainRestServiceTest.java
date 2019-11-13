package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.mapper.SupplyChainMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class SupplyChainRestServiceTest {

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private SupplyChainMapper converter;

    @Test
    void createSupplyChain_WithUniqueName_shouldBe_succesfull() {

    }

    @Test
    void getSupplyChain() {
    }

    @Test
    void searchSupplyChains() {
    }
}