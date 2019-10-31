package com.rabobank.argos.argos4j.internal.mapper;


import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LinkMetaBlockMapper {

    LinkMetaBlockMapper INSTANCE = Mappers.getMapper(LinkMetaBlockMapper.class);

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}