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

import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckDataExtractorTest {

    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    @Mock
    private ReflectionHelper reflectionHelper;
    @Mock
    private Method method;

    @Mock
    private ApplicationContext applicationContext;


    private DefaultLocalPermissionCheckDataExtractor defaultLocalPermissionCheckDataExtractor;

    @BeforeEach
    void setup() {
        defaultLocalPermissionCheckDataExtractor = new DefaultLocalPermissionCheckDataExtractor(reflectionHelper, applicationContext);

    }


}