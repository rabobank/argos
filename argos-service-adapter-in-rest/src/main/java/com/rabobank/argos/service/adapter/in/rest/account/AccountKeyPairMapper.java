/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccountKeyPair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static com.rabobank.argos.domain.key.RSAPublicKeyFactory.instance;

@Mapper(componentModel = "spring")
public abstract class AccountKeyPairMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(source = "hashedKeyPassphrase", target = "encryptedHashedKeyPassphrase", qualifiedByName = "encryptHashedKeyPassphrase")
    public abstract NonPersonalAccountKeyPair convertFromRestKeyPair(RestNonPersonalAccountKeyPair restKeyPair);

    public abstract RestNonPersonalAccountKeyPair convertToRestKeyPair(NonPersonalAccountKeyPair keyPair);

    public abstract KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);

    public abstract RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    @Named("encryptHashedKeyPassphrase")
    public String encryptHashedKeyPassphrase(String hashedKeyPassphrase) {
        return passwordEncoder.encode(hashedKeyPassphrase);
    }

    @Mapping(source = "publicKey", target = "publicKey")
    public PublicKey convertByteArrayToPublicKey(byte[] publicKey) {
        try {
            return instance(publicKey);
        } catch (GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid public key " + e.getMessage());
        }
    }

    @Mapping(source = "publicKey", target = "publicKey")
    public byte[] convertPublicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
