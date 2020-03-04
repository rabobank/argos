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
import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.rabobank.argos.service.domain.security.DefaultLocalPermissionCheckDataExtractor.DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;

@Component(DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
public class DefaultLocalPermissionCheckDataExtractor implements LocalPermissionCheckDataExtractor {
    public static final String DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME = "defaultLocalPermissionCheckDataExtractor";
    private final ReflectionHelper reflectionHelper;

    @Override
    public LocalPermissionCheckData extractLocalPermissionCheckData(Method method, Object[] argumentValues) {

        LocalPermissionCheckData.LocalPermissionCheckDataBuilder builder = LocalPermissionCheckData.builder();
        reflectionHelper.getParameterDataByAnnotation(method,
                LabelIdCheckParam.class,
                argumentValues).ifPresent(parameterData ->
                builder.labelId(getValue(
                        parameterData.getValue(),
                        parameterData.getAnnotation().propertyPath()
                        )
                )
        );

        reflectionHelper.getParameterDataByAnnotation(method,
                ParentLabelIdCheckParam.class,
                argumentValues).ifPresent(parameterData ->
                builder.parentLabelId(getValue(parameterData.getValue(),
                        parameterData.getAnnotation()
                                .propertyPath()
                        )
                )
        );
        return builder.build();
    }

    private String getValue(Object value, String path) {
        if (StringUtils.isEmpty(path)) {
            return (String) value;
        } else {
            try {
                return BeanUtilsBean.getInstance().getProperty(value, path);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new ArgosError(e.getMessage(), e);
            }
        }
    }

}
