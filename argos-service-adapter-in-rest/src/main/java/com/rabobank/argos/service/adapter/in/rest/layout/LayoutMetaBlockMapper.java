package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {StepMapper.class})
public interface LayoutMetaBlockMapper {

    LayoutMetaBlock convertFromRestLayoutMetaBlock(RestLayoutMetaBlock metaBlock);

    @Mapping(source = "layoutMetaBlockId", target = "id")
    RestLayoutMetaBlock convertToRestLayoutMetaBlock(LayoutMetaBlock metaBlock);
}
