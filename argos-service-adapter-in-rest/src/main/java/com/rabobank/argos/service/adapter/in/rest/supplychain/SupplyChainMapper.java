package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplyChainMapper {

    SupplyChain convertFromRestSupplyChainCommand(RestCreateSupplyChainCommand createSupplyChainCommand);

    @Mapping(source = "supplyChainId", target = "id")
    RestSupplyChainItem convertToRestRestSupplyChainItem(SupplyChain supplyChain);
}
