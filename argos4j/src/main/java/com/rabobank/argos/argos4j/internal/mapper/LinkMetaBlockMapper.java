package com.rabobank.argos.argos4j.internal.mapper;


import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {SignatureMapper.class})
public interface LinkMetaBlockMapper {

    LinkMetaBlockMapper INSTANCE = Mappers.getMapper(LinkMetaBlockMapper.class);

    @Mappings({
            @Mapping(source = "signed", target = "link")
    })
    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    @InheritInverseConfiguration
    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}