package com.rabobank.argos.argos4j.internal.mapper;

import com.rabobank.argos.argos4j.rest.api.model.RestSignature;
import com.rabobank.argos.domain.model.Signature;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface SignatureMapper {

    @Mappings({
            @Mapping(source = "sig", target = "signature"),
            @Mapping(source = "keyId", target = "keyId")
    })
    Signature toSignature(RestSignature restSignature);

    @InheritInverseConfiguration
    RestSignature fromSignature(Signature signature);
}
