/*
 * Copyright (C) 2019 Rabobank Nederland
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
package com.rabobank.argos.integrationtest.service.layout;

import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestRule;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchRuleMapper {

    @Mapping(source = "destinationPathPrefix", target = "destinationPathPrefix")
    MatchRule mapFromRestRule(RestRule restRule);

    @InheritInverseConfiguration
    RestRule mapToRestRule(MatchRule rule);
}
