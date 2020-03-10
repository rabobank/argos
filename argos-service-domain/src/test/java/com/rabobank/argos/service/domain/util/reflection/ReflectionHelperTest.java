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
package com.rabobank.argos.service.domain.util.reflection;

import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class ReflectionHelperTest {

    public static final String LABEL_ID = "labelId";

    @Test
    void getParameterDataByAnnotation() throws NoSuchMethodException {
        Stream<ParameterData<LabelIdCheckParam, Object>> data = new ReflectionHelper().getParameterDataByAnnotation(TestClass.class.getMethod("test", String.class, String.class), LabelIdCheckParam.class, new Object[]{"arg1", LABEL_ID});
        ParameterData<LabelIdCheckParam, Object> labelIdCheckParamObjectParameterData = data.findFirst().orElseThrow();
        assertThat(labelIdCheckParamObjectParameterData.getValue(), is(LABEL_ID));
        assertThat(labelIdCheckParamObjectParameterData.getAnnotation().propertyPath(), is(""));
    }

    @Test
    void getParameterDataByAnnotationNotFound() throws NoSuchMethodException {
        assertThat(new ReflectionHelper().getParameterDataByAnnotation(TestClass.class.getMethod("testWithoutAnnotation", String.class, String.class), LabelIdCheckParam.class, new Object[]{"arg1", LABEL_ID}).collect(Collectors.toList()), empty());
    }

    private class TestClass {
        public void test(String arg1, @LabelIdCheckParam String labelId) {

        }

        public void testWithoutAnnotation(String arg1, String labelId) {

        }
    }
}