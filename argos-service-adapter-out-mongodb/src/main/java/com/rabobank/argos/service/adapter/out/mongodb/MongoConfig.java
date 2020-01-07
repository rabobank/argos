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

import com.rabobank.argos.service.adapter.out.mongodb.key.converter.ByteArrayToPublicKeyToReadConverter;
import com.rabobank.argos.service.adapter.out.mongodb.key.converter.PublicKeyToByteArrayWriteConverter;
import com.rabobank.argos.service.adapter.out.mongodb.layout.converter.MatchFilterReadConverter;
import com.rabobank.argos.service.adapter.out.mongodb.layout.converter.MatchFilterWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new ByteArrayToPublicKeyToReadConverter());
        converterList.add(new PublicKeyToByteArrayWriteConverter());
        converterList.add(new MatchFilterWriteConverter());
        converterList.add(new MatchFilterReadConverter());
        return new MongoCustomConversions(converterList);
    }
}
