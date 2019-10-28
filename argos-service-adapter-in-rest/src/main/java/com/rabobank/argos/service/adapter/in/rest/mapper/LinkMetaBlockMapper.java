package com.rabobank.argos.service.adapter.in.rest.mapper;


import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {SignatureMapper.class})
public interface LinkMetaBlockMapper {

    @Mappings({
            @Mapping(source = "signed", target = "link")
    })
    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    @InheritInverseConfiguration
    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}