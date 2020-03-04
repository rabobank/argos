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
package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.layout.rule.Rule;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.ArrayList;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Step {
    private String name;
    @Builder.Default
    private List<String> authorizedKeyIds = new ArrayList<>();
    private int requiredNumberOfLinks;
    @Builder.Default
    private List<String> expectedCommand = new ArrayList<>();
    @Builder.Default
    private List<Rule> expectedMaterials = new ArrayList<>();
    @Builder.Default
    private List<Rule> expectedProducts = new ArrayList<>();
}
