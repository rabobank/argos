package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class LinkMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return VerificationRunResult.builder().runIsValid(context.getLinkMetaBlocks().stream().map(this::verify).allMatch(result -> result)).build();
    }

    private boolean verify(LinkMetaBlock linkMetaBlock) {
        return keyPairRepository.findByKeyId(linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> signatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature().getSignature(), keyPair.getPublicKey()))
                .orElse(false);
    }


}
