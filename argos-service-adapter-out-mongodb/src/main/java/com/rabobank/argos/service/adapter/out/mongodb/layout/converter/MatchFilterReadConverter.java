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
package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class MatchFilterReadConverter implements Converter<DBObject, MatchFilter> {

    @Override
    public MatchFilter convert(DBObject dbObject) {
        return MatchFilter.builder()
                .destinationStepName((String) dbObject.get("destinationStepName"))
                .pattern((String) dbObject.get("pattern"))
                .destinationType(DestinationType.valueOf((String) dbObject.get("destinationType")))
                .build();

    }
}
