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
package com.rabobank.argos.service.adapter.out.mongodb;

import com.github.mongobee.Mongobee;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PublicKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;


@ExtendWith(MockitoExtension.class)
class MongoConfigTest {

    private MongoConfig config;

    @Mock
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        config = new MongoConfig();
    }

    @Test
    void customConversions() {
        MongoCustomConversions conversions = config.customConversions();
        assertThat(conversions.hasCustomReadTarget(Binary.class, PublicKey.class), is(true));
        assertThat(conversions.hasCustomWriteTarget(PublicKey.class, byte[].class), is(true));
    }

    @Test
    void mongobee() {
        ReflectionTestUtils.setField(config, "mongoURI", "mongodb://localhost/test");
        Mongobee mongobee = config.mongobee(mongoTemplate);

        assertThat(ReflectionTestUtils.getField(mongobee, "changeLogsScanPackage"), is("com.rabobank.argos.service.adapter.out.mongodb"));
        assertThat(ReflectionTestUtils.getField(mongobee, "mongoTemplate"), sameInstance(mongoTemplate));

    }
}