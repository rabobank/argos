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
package com.rabobank.argos.integrationtest.service.layout;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestLayoutMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static com.rabobank.argos.domain.key.RSAPublicKeyFactory.instance;


@Mapper(componentModel = "spring", uses = {StepMapper.class})
public interface LayoutMetaBlockMapper {


    @Mapping(target = "layoutMetaBlockId", ignore = true)
    @Mapping(target = "supplyChainId", ignore = true)
    LayoutMetaBlock convertFromRestLayoutMetaBlock(RestLayoutMetaBlock metaBlock);

    @Mapping(source = "layoutMetaBlockId", target = "id")
    RestLayoutMetaBlock convertToRestLayoutMetaBlock(LayoutMetaBlock metaBlock);

    @Mapping(source = "key", target = "key")
    default PublicKey convertByteArrayToPublicKey(byte[] publicKey) {
        try {
            return instance(publicKey);
        } catch (GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid public key " + e.getMessage());
        }
    }

    @Mapping(source = "key", target = "key")
    default byte[] convertPublicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
