package com.rabobank.argos.service.domain.verification.rules;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
    private final Link link;

    public Stream<Artifact> getFilteredProducts() {
        return filterArtifacts(link.getProducts());
    }

    public Stream<Artifact> getFilteredMaterials() {
        return filterArtifacts(link.getMaterials());
    }

    private Stream<Artifact> filterArtifacts(List<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + rule.getPattern());
        return artifacts.stream().filter(artifact -> matcher.matches(Paths.get(artifact.getUri())));
    }

    public boolean containsSomeMaterials(List<Artifact> artifacts) {
        return artifacts.stream().anyMatch(artifact -> link.getMaterials().contains(artifact));
    }
}
