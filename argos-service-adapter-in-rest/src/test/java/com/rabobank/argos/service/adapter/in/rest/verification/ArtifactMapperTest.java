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
package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

class ArtifactMapperTest {

    public static final String HASH = "hash";
    public static final String URI = "uri";
    private ArtifactMapper artifactMapper;

    @BeforeEach
    public void setup() {
        artifactMapper = Mappers.getMapper(ArtifactMapper.class);
    }

    @Test
    void mapToArtifactsShould_Return_Artifacts() {
        RestArtifact restArtifact = new RestArtifact();
        restArtifact.setHash(HASH);
        restArtifact.setUri(URI);
        List<Artifact> artifacts = artifactMapper.mapToArtifacts(singletonList(restArtifact));
        assertThat(artifacts, hasSize(1));
        assertThat(artifacts.iterator().next().getHash(), is(HASH));
        assertThat(artifacts.iterator().next().getUri(), is(URI));
    }

}