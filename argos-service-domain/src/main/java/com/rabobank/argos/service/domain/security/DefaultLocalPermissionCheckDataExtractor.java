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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.domain.security.DefaultLocalPermissionCheckDataExtractor.DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;

@Component(DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME)
@RequiredArgsConstructor
public class DefaultLocalPermissionCheckDataExtractor implements LocalPermissionCheckDataExtractor {
    public static final String DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME = "defaultLocalPermissionCheckDataExtractor";
    private final ReflectionHelper reflectionHelper;

    private final ApplicationContext applicationContext;

    @Override
    public LocalPermissionCheckData extractLocalPermissionCheckData(Method method, Object[] argumentValues) {

        LocalPermissionCheckData.LocalPermissionCheckDataBuilder builder = LocalPermissionCheckData.builder();
        builder.labelIds(
                reflectionHelper.getParameterDataByAnnotation(method, LabelIdCheckParam.class, argumentValues)
                        .map(parameterData -> getValue(parameterData.getValue(), parameterData.getAnnotation()
                        )).flatMap(Optional::stream).collect(Collectors.toSet()));
        return builder.build();
    }

    private Optional<String> getValue(Object value, LabelIdCheckParam checkParam) {
        LabelIdExtractor labelIdExtractor = applicationContext.getBean(checkParam.dataExtractor(), LabelIdExtractor.class);
        return labelIdExtractor.extractLabelId(checkParam, value);

    }

}
