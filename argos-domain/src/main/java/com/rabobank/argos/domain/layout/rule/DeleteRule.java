package com.rabobank.argos.domain.layout.rule;

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

import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class DeleteRule extends Rule {
    @Builder
    public DeleteRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        // Consume filtered artifacts that are products but not materials
        // (materials - products)
        Set<Artifact> deletedArtifacts = new HashSet<>();
        deletedArtifacts.addAll(materials);
        if (products != null) {
            deletedArtifacts.removeAll(products);
        }
        return filteredArtifacts.stream().filter(deletedArtifacts::contains)
                .collect(Collectors.toSet());
    }
}
