package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring", uses = {StepMapper.class})
public interface LayoutMetaBlockMapper {

    LayoutMetaBlock convertFromRestLayoutMetaBlock(RestLayoutMetaBlock metaBlock);

    RestLayoutMetaBlock convertToRestLayoutMetaBlock(LayoutMetaBlock metaBlock);
}