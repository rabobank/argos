package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class LayoutMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verify(context.getLayoutMetaBlock());
    }

    private VerificationRunResult verify(LinkMetaBlock linkMetaBlock) {
        return keyPairRepository.findByKeyId(linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> signatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature().getSignature(), keyPair.getPublicKey()))
                .map(result -> VerificationRunResult.builder().runIsValid(result).build())
                .orElse(VerificationRunResult.notOkay());
    }

    private VerificationRunResult verify(LayoutMetaBlock layoutMetaBlock) {
        return VerificationRunResult.builder()
                .runIsValid(layoutMetaBlock.getSignatures().stream().allMatch(signature -> keyPairRepository.findByKeyId(signature.getKeyId())
                        .map(keyPair -> signatureValidator.isValid(layoutMetaBlock.getLayout(), signature.getSignature(), keyPair.getPublicKey())).orElse(false)))
                .build();

    }
}
