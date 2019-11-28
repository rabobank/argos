package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;


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
        context.removeLinkMetaBlocks(context.getLinkMetaBlocks().stream().filter(linkMetaBlock -> !okay(linkMetaBlock)).collect(toList()));
        return VerificationRunResult.okay();
    }

    private boolean okay(LinkMetaBlock linkMetaBlock) {
        return keyPairRepository.findByKeyId(linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> signatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature().getSignature(), keyPair.getPublicKey()))
                .orElse(false);
    }


}
