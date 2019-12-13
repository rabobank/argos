/*
 * Copyright (C) 2019 Rabobank Nederland
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

import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.handler.KeyApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class KeyRestService implements KeyApi {

    private final KeyPairMapper converter;
    private final KeyPairRepository keyPairRepository;
    private final KeyIdProvider keyIdProvider = new KeyIdProviderImpl();

    @Override
    public ResponseEntity<RestKeyPair> getKey(String keyId) {
        KeyPair keyPair = keyPairRepository.findByKeyId(keyId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "key pair not found : " + keyId)
        );
        return new ResponseEntity<>(converter.convertToRestKeyPair(keyPair), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> storeKey(RestKeyPair restKeyPair) {
        KeyPair keyPair = converter.convertFromRestKeyPair(restKeyPair);
        validateKeyId(keyPair);
        keyPairRepository.save(keyPair);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(keyIdProvider.computeKeyId(keyPair.getPublicKey()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }
}
