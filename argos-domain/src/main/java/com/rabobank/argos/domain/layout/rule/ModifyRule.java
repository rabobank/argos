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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class ModifyRule extends Rule {
    @Builder
    public ModifyRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        Set<Artifact> consumed = new HashSet<>();
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        Map<String, Artifact> productsMap = createMap(products);
        Map<String, Artifact> materialsMap = createMap(materials);
        for (Artifact artifact : filteredArtifacts) {
            // Consume filtered artifacts that have different hashes
            if (productsMap.containsKey(artifact.getUri())
                    && materialsMap.containsKey(artifact.getUri())
                    && (!productsMap.get(artifact.getUri()).getHash().equals(materialsMap.get(artifact.getUri()).getHash()))) {
                consumed.add(artifact);
            }
        }
        return consumed;
    }

    private static Map<String, Artifact> createMap(Set<Artifact> artifacts) {
        Map<String, Artifact> result = new HashMap<>();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                result.put(artifact.getUri(), artifact);
            }
        }
        return result;
    }

}
