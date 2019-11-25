package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SignatureValidator {

    public boolean isValid(Link link, String signature, PublicKey publicKey) {
        return isValid(new JsonSigningSerializer().serialize(link), signature, publicKey);
    }

    public boolean isValid(Layout layout, String signature, PublicKey publicKey) {
        return isValid(new JsonSigningSerializer().serialize(layout), signature, publicKey);
    }

    private boolean isValid(String signableJson, String signature, PublicKey publicKey) {
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(signableJson.getBytes(UTF_8));
            byte[] signatureBytes = Hex.decodeHex(signature);
            return publicSignature.verify(signatureBytes);
        } catch (GeneralSecurityException | DecoderException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
}
