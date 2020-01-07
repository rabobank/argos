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
package com.rabobank.argos.service.adapter.out.mongodb.key.converter;

import com.rabobank.argos.service.adapter.out.mongodb.MongoDbException;
import org.bson.types.Binary;
import org.springframework.core.convert.converter.Converter;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static com.rabobank.argos.domain.key.RSAPublicKeyFactory.instance;

public class ByteArrayToPublicKeyToReadConverter implements Converter<Binary, PublicKey> {

    @Override
    public PublicKey convert(Binary bytes) {
        try {
            return instance(bytes.getData());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new MongoDbException(e);
        }
    }
}
