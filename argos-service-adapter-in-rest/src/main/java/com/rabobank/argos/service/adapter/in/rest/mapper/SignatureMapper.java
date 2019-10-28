package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.rabobank.argos.domain.model.Signature;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSignature;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SignatureMapper {

    @Mappings({
            @Mapping(source = "sig", target = "signature"),
            @Mapping(source = "keyId", target = "keyId")
    })
    Signature toSignature(RestSignature restSignature);

    @InheritInverseConfiguration
    RestSignature fromSignature(Signature signature);
}
