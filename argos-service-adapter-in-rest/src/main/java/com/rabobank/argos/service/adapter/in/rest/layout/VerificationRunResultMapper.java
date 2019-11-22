package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.domain.VerificationRunProvider.VerificationRunResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerificationRunResultMapper {
    RestVerificationResult mapToRestVerificationResult(VerificationRunResult verificationRunResult);

}
