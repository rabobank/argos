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

import com.codepoetics.protonpack.StreamUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Component
public class ReflectionHelper {


    public List<ParameterData> getParameterDataByType(Method method, Class type, Object[] argumentValues) {
        return StreamUtils
                .zipWithIndex(asList(method.getParameters()).stream())
                .filter(p -> p.getValue().getType().equals(type))
                .map(p -> ParameterData
                        .builder()
                        .parameter(p.getValue())
                        .value(getArgumentValueByIndex(argumentValues, (int) p.getIndex()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<ParameterData> getParameterDataByAnnotation(Method method, Class<? extends Annotation> annotation, Object[] argumentValues) {
        return StreamUtils
                .zipWithIndex(Arrays.stream(method.getParameters()))
                .filter(p -> p.getValue().getAnnotation(annotation) != null)
                .map(p -> ParameterData
                        .builder()
                        .parameter(p.getValue())
                        .value(getArgumentValueByIndex(argumentValues, (int) p.getIndex()))
                        .annotation(p.getValue().getAnnotation(annotation))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static Object getArgumentValueByIndex(Object[] argumentValues, Integer index) {
        try {
            return argumentValues[index];
        } catch (Exception e) {
            throw new IllegalArgumentException("argument value at index: " + index + " does not exist", e);
        }
    }

}
