package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerificationResultMapper {
    RestVerificationResult mapToRestVerificationResult(VerificationRunResult verificationRunResult);
}
