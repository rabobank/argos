package com.rabobank.argos.service.adapter.out.mongodb.converter;

import org.springframework.core.convert.converter.Converter;

import java.security.PublicKey;

public class PublicKeyToByteArrayWriteConverter implements Converter<PublicKey, byte[]> {
    @Override
    public byte[] convert(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
