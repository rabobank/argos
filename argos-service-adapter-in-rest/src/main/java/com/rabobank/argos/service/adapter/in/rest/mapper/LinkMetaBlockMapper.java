package com.rabobank.argos.service.adapter.in.rest.mapper;


import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMetaBlockMapper {

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}