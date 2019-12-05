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

import com.rabobank.argos.domain.layout.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.link.Artifact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public abstract class Rule {

    private String pattern;

    public abstract Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError;

    protected Set<Artifact> filterArtifacts(Set<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.getPattern());
        Set<Artifact> filteredArtifacts = new HashSet<>();
        Iterator<Artifact> artifactIterator = artifacts.iterator();
        while (artifactIterator.hasNext()) {
            Artifact artifact = artifactIterator.next();
            if (matcher.matches(Paths.get(artifact.getUri()))) {
                filteredArtifacts.add(artifact);
            }
        }
        return filteredArtifacts;
    }
}
