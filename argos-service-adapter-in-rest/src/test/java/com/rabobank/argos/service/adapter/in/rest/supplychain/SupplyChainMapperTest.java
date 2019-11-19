package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.core.Is.is;

class SupplyChainMapperTest {

    private static final String NAME = "name";
    private static final String ID = "ID";
    private SupplyChainMapper supplyChainMapper;

    @BeforeEach
    public void setup() {
        supplyChainMapper = Mappers.getMapper(SupplyChainMapper.class);
    }

    @Test
    void convertFromRestSupplyChainCommand_Should_Return_SupplyChain() {
        RestCreateSupplyChainCommand restCreateSupplyChainCommand = new RestCreateSupplyChainCommand();
        restCreateSupplyChainCommand.name(NAME);
        SupplyChain supplyChain = supplyChainMapper.convertFromRestSupplyChainCommand(restCreateSupplyChainCommand);
        assertThat(supplyChain.getName(), is(NAME));
        assertThat(supplyChain.getSupplyChainId(), hasLength(36));
    }

    @Test
    void convertToRestRestSupplyChainItem_Should_Return_RestSupplyChainItem() {
        SupplyChain supplyChain = SupplyChain.builder().name(NAME).supplyChainId(ID).build();
        RestSupplyChainItem restSupplyChainItem = supplyChainMapper.convertToRestRestSupplyChainItem(supplyChain);
        assertThat(restSupplyChainItem.getName(), is(NAME));
        assertThat(restSupplyChainItem.getId(), is(ID));
    }
}