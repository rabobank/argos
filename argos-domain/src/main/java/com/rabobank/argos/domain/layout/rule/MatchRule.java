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
package com.rabobank.argos.domain.layout.rule;


import com.rabobank.argos.domain.layout.DestinationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MatchRule extends Rule {

    private String sourcePathPrefix;
    private String destinationPathPrefix;
    private DestinationType destinationType;
    private String destinationStepName;

    @Builder
    public MatchRule(String pattern, String destinationPathPrefix, String sourcePathPrefix, DestinationType destinationType,
                     String destinationStepName) {
        super(RuleType.MATCH, pattern);
        this.sourcePathPrefix = sourcePathPrefix;
        this.destinationPathPrefix = destinationPathPrefix;
        this.destinationType = destinationType;
        this.destinationStepName = destinationStepName;
    }


}


