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
package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Builder
@Getter
@Slf4j
public class RuleVerificationContext<R extends Rule> {

    private final VerificationContext verificationContext;
    private final R rule;
    private final List<Artifact> materials;
    private final List<Artifact> products;

    public Stream<Artifact> getFilteredProducts() {
        return getFilteredProducts(null);
    }

    public Stream<Artifact> getFilteredMaterials() {
        return getFilteredMaterials(null);
    }

    public Stream<Artifact> getFilteredProducts(String prefix) {
        return filterArtifacts(products, rule.getPattern(), prefix);
    }

    public Stream<Artifact> getFilteredMaterials(String prefix) {
        return filterArtifacts(materials, rule.getPattern(), prefix);
    }

    public static Stream<Artifact> filterArtifacts(List<Artifact> artifacts, String pattern, @Nullable String prefix) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return artifacts.stream().filter(artifact -> matcher.matches(Paths.get(getUri(artifact, prefix))));
    }

    private static String getUri(Artifact artifact, String prefix) {
        if (StringUtils.hasLength(prefix) && artifact.getUri().startsWith(prefix)) {
            return artifact.getUri().substring(prefix.length());
        } else {
            return artifact.getUri();
        }
    }

    public boolean containsSomeMaterials(List<Artifact> artifacts) {
        return artifacts.stream().anyMatch(materials::contains);
    }

    public <T extends Rule> T getRule() {
        return (T) rule;
    }
}
