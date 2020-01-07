/*
 * Copyright (C) 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest.key;

import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static com.rabobank.argos.domain.key.RSAPublicKeyFactory.instance;

@Mapper(componentModel = "spring")
public interface KeyPairMapper {

    KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);

    RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    @Mapping(source = "publicKey", target = "publicKey")
    default PublicKey convertByteArrayToPublicKey(byte[] publicKey) {
        try {
            return instance(publicKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid public key " + e.getMessage());
        }
    }

    @Mapping(source = "publicKey", target = "publicKey")
    default byte[] convertPublicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
