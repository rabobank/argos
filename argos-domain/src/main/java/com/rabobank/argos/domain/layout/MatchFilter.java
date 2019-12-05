/**
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
package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.link.Artifact;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Setter
@Getter
@ToString
public class MatchFilter {

    private String pattern;
    private DestinationType destinationType;
    private String destinationStepName;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private final PathMatcher matcher;

    @Builder
    public MatchFilter(String pattern, DestinationType destinationType, String destinationStepName) {
        this.pattern = pattern;
        this.destinationType = destinationType;
        this.destinationStepName = destinationStepName;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

    }

    public boolean matchUri(String uri) {
        return matcher.matches(Paths.get(uri));
    }

    public List<Artifact> matches(List<Artifact> productsToVerify) {
        return productsToVerify.stream().filter(artifact -> matchUri(artifact.getUri())).collect(toList());
    }

}
