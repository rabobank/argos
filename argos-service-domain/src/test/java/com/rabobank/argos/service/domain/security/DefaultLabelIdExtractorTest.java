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
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.hierarchy.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLabelIdExtractorTest {

    private static final String VALUE = "value";

    private DefaultLabelIdExtractor extractor;

    @Mock
    private LabelIdCheckParam checkParam;

    @BeforeEach
    void setUp() {
        extractor = new DefaultLabelIdExtractor();
    }

    @Test
    void extractLabelId() {
        assertThat(extractor.extractLabelId(checkParam, VALUE), is(Optional.of(VALUE)));
    }

    @Test
    void extractLabelFromObject() {
        when(checkParam.propertyPath()).thenReturn("labelId");
        assertThat(extractor.extractLabelId(checkParam, Label.builder().labelId(VALUE).build()), is(Optional.of(VALUE)));
    }

    @Test
    void extractLabelFromObjectMethodNotExists() {
        when(checkParam.propertyPath()).thenReturn("other");
        ArgosError argosError = assertThrows(ArgosError.class, () -> extractor.extractLabelId(checkParam, Label.builder().labelId(VALUE).build()));
        assertThat(argosError.getMessage(), is("Unknown property 'other' on class 'class com.rabobank.argos.domain.hierarchy.Label'"));
    }

}