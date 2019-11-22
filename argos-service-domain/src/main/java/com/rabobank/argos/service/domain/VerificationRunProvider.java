package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.repository.KeyPairRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VerificationRunProvider {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;


    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> expectedProducts) {

        return VerificationRunResult.builder().runIsValid(true).build();
    }

    @Getter
    @Builder
    public static class VerificationRunResult {
        private boolean runIsValid = false;
        private List<String> messages;
    }


}
