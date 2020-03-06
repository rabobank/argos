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
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Component(DefaultLabelIdExtractor.DEFAULT_LABEL_ID_EXTRACTOR)
public class DefaultLabelIdExtractor implements LabelIdExtractor {
    public static final String DEFAULT_LABEL_ID_EXTRACTOR = "defaultLabelIdExtractor";

    @Override
    public Optional<String> extractLabelId(LabelIdCheckParam checkParam, Object value) {
        return Optional.ofNullable(getValue(value, checkParam.propertyPath()));
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
