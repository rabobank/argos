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

import com.rabobank.argos.service.domain.util.reflection.ParameterData;
import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckDataExtractorTest {

    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    public static final Object[] ARGUMENT_VALUES = {LABEL_ID, PARENT_LABEL_ID};
    private static final String EXTRACTOR = "extratcor";
    @Mock
    private ReflectionHelper reflectionHelper;
    @Mock
    private Method method;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ParameterData<LabelIdCheckParam, Object> parameterData;

    @Mock
    private LabelIdCheckParam labelIdCheckParam;

    @Mock
    private LabelIdExtractor labelIdExtractor;


    private DefaultLocalPermissionCheckDataExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new DefaultLocalPermissionCheckDataExtractor(reflectionHelper, applicationContext);
    }

    @Test
    void extractLocalPermissionCheckData() {
        when(reflectionHelper.getParameterDataByAnnotation(method, LabelIdCheckParam.class, ARGUMENT_VALUES)).thenReturn(Stream.of(parameterData));
        when(parameterData.getAnnotation()).thenReturn(labelIdCheckParam);
        when(parameterData.getValue()).thenReturn(PARENT_LABEL_ID);
        when(labelIdCheckParam.dataExtractor()).thenReturn(EXTRACTOR);
        when(applicationContext.getBean(EXTRACTOR, LabelIdExtractor.class)).thenReturn(labelIdExtractor);
        when(labelIdExtractor.extractLabelId(labelIdCheckParam, PARENT_LABEL_ID)).thenReturn(Optional.of(LABEL_ID));
        LocalPermissionCheckData checkData = extractor.extractLocalPermissionCheckData(method, ARGUMENT_VALUES);
        assertThat(checkData.getLabelIds(), contains(LABEL_ID));
    }
}