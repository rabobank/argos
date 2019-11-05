package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeyPairMapper {
    KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);

    RestKeyPair convertToRestKeyPair(KeyPair keyPair);
}
