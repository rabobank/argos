/*
 * Copyright (C) 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {StepMapper.class})
public interface LayoutMetaBlockMapper {

    LayoutMetaBlock convertFromRestLayoutMetaBlock(RestLayoutMetaBlock metaBlock);

    @Mapping(source = "layoutMetaBlockId", target = "id")
    RestLayoutMetaBlock convertToRestLayoutMetaBlock(LayoutMetaBlock metaBlock);
}
