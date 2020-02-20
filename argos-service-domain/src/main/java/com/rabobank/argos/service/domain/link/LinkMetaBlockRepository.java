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
package com.rabobank.argos.service.domain.link;


import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public interface LinkMetaBlockRepository {
    List<LinkMetaBlock> findBySupplyChainId(String supplyChainId);
    List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash);
    void save(LinkMetaBlock link);

    List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(String supplyChainId, String segmentName, String stepName, List<String> hashes);

    List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(String supplyChainId, String segmentName, String stepName, List<String> hashes);

    List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(String supplyChainId, String segmentName, String stepName, EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes);

    List<LinkMetaBlock> findByRunId(String supplyChainId, String runId);

    List<LinkMetaBlock> findByRunId(String supplyChainId, String segmentName, String runId, Set<String> resolvedSteps);
}
