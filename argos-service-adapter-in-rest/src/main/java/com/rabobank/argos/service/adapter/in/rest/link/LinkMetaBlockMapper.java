package com.rabobank.argos.service.adapter.in.rest.link;


import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMetaBlockMapper {

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}
