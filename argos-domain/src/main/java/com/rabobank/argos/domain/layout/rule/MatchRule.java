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


import com.rabobank.argos.domain.layout.ArtifactType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MatchRule extends Rule {

    private String sourcePathPrefix;
    private ArtifactType destinationType;
    private String destinationPathPrefix;
    private String destinationSegmentName;
    private String destinationStepName;

    @Builder
    public MatchRule(String pattern, String sourcePathPrefix, @NonNull ArtifactType destinationType,
            String destinationPathPrefix, String destinationSegmentName, String destinationStepName) {
        super(RuleType.MATCH, pattern);
        this.sourcePathPrefix = sourcePathPrefix;
        this.destinationPathPrefix = destinationPathPrefix;
        this.destinationType = destinationType;
        this.destinationSegmentName = destinationSegmentName;
        this.destinationStepName = destinationStepName;
    }
}


