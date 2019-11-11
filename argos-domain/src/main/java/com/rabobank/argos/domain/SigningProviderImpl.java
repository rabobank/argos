package com.rabobank.argos.domain;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.PublicKey;

public class SigningProviderImpl implements SigningProvider{

    @Override
    public String computeKeyId(PublicKey publicKey){
        return DigestUtils.sha256Hex(publicKey.getEncoded());
    }
}
