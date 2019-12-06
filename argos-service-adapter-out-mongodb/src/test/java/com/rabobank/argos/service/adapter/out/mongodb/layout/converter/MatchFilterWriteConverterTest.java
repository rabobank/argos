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
package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class MatchFilterWriteConverterTest {

    private static final String ARTIFACT_JAVA = "/artifact.java";
    private static final String STEP_NAME = "StepName";
    private MatchFilterWriteConverter matchFilterWriteConverter;
    private MatchFilter matchFilter;

    @BeforeEach
    public void setup() {
        matchFilterWriteConverter = new MatchFilterWriteConverter();
        matchFilter = MatchFilter
                .builder()
                .pattern(ARTIFACT_JAVA)
                .destinationStepName(STEP_NAME)
                .destinationType(DestinationType.PRODUCTS)
                .build();

    }

    @Test
    void testConvert() {
        DBObject dbObject = matchFilterWriteConverter.convert(matchFilter);
        assertThat(dbObject.get("pattern"), is(ARTIFACT_JAVA));
        assertThat(dbObject.get("destinationStepName"), is(STEP_NAME));
        assertThat(dbObject.get("destinationType"), is("PRODUCTS"));
    }
}
