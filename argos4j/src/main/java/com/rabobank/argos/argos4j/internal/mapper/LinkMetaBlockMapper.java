package com.rabobank.argos.argos4j.internal.mapper;


import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LinkMetaBlockMapper {

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}