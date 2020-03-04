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
package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.service.domain.security.LocalPermissionCheckData;
import com.rabobank.argos.service.domain.security.LocalPermissionCheckDataExtractor;
import com.rabobank.argos.service.domain.security.ParentLabelIdCheckParam;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component(SupplyChainLocalPermissionCheckDataExtractor.SUPPLY_CHAIN_LOCAL_DATA_EXTRACTOR)
@RequiredArgsConstructor
public class SupplyChainLocalPermissionCheckDataExtractor implements LocalPermissionCheckDataExtractor {
    public static final String SUPPLY_CHAIN_LOCAL_DATA_EXTRACTOR = "SupplyChainLocalPermissionCheckDataExtractor";

    private final ReflectionHelper reflectionHelper;

    private final SupplyChainRepository supplyChainRepository;

    @Override
    public LocalPermissionCheckData extractLocalPermissionCheckData(Method method, Object[] argumentValues) {
        LocalPermissionCheckData.LocalPermissionCheckDataBuilder builder = LocalPermissionCheckData.builder();
        reflectionHelper.getParameterDataByAnnotation(method,
                ParentLabelIdCheckParam.class,
                argumentValues).ifPresent(parameterData -> builder.parentLabelId(getParentLabelId((String) parameterData.getValue()))
        );
        return builder.build();
    }

    private String getParentLabelId(String supplyChainId) {
        Optional<String> optionalParentLabelId = supplyChainRepository.findParentLabelIdBySupplyChainId(supplyChainId);
        return optionalParentLabelId.orElse(null);
    }
}
