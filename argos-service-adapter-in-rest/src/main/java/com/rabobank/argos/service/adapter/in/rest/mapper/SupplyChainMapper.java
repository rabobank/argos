package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.function.Supplier;

@Mapper(componentModel = "spring")
public interface SupplyChainMapper {
    default SupplyChain convertFromRestSupplyChainCommand(RestCreateSupplyChainCommand createSupplyChainCommand, @Context Supplier<String> idGenerator) {
        return SupplyChain
                .builder()
                .supplyChainId(idGenerator.get())
                .name(createSupplyChainCommand.getName())
                .build();
    }

    @Mapping(source = "supplyChainId", target = "id")
    RestSupplyChainItem convertToRestRestSupplyChainItem(SupplyChain supplyChain);
}
