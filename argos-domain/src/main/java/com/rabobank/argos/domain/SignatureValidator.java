package com.rabobank.argos.domain;

import com.rabobank.argos.domain.model.LinkMetaBlock;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SignatureValidator {

    public boolean isValid(LinkMetaBlock linkMetaBlock, PublicKey publicKey) {
        try {
            String linkJson = new JsonSigningSerializer().serialize(linkMetaBlock.getLink());
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(linkJson.getBytes(UTF_8));
            byte[] signatureBytes = Hex.decodeHex(linkMetaBlock.getSignature().getSignature());
            return publicSignature.verify(signatureBytes);
        } catch (GeneralSecurityException | DecoderException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
}
