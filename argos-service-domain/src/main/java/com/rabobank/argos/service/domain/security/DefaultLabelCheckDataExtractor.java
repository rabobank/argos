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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultLabelCheckDataExtractor implements LabelCheckDataExtractor {
    private final BeanUtilsBean beanutils = new BeanUtilsBean();
    private final ReflectionHelper reflectionHelper;

    @Override
    public Optional<LabelCheckData> extractLabelCheckData(Method method, Object[] argumentValues) {

        List<ParameterData> labelIdCheckParameterData = reflectionHelper.getParameterDataByAnnotation(method,
                LabelIdCheckParam.class,
                argumentValues);

        LabelCheckData.LabelCheckDataBuilder builder = LabelCheckData.builder();
        if (!labelIdCheckParameterData.isEmpty()) {

            ParameterData<LabelIdCheckParam, String> parameter = labelIdCheckParameterData.iterator().next();

            if (StringUtils.isEmpty(parameter.getAnnotation().propertyPath())) {
                builder.labelId(parameter.getValue());
            } else {
                try {
                    String value = beanutils.
                            getProperty(parameter.getValue(),
                                    parameter.getAnnotation().propertyPath());
                    builder.labelId(value);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    log.error("error {}", e);
                }
            }
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }


    }

}
