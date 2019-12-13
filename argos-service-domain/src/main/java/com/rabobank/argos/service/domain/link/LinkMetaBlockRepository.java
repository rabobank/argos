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
package com.rabobank.argos.service.domain.link;


import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.List;

public interface LinkMetaBlockRepository {
    List<LinkMetaBlock> findBySupplyChainId(String supplyChainId);
    List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash);
    void save(LinkMetaBlock link);

    List<LinkMetaBlock> findBySupplyChainAndStepNameAndProductHashes(String supplyChainId, String stepName, List<String> hashes);

    List<LinkMetaBlock> findBySupplyChainAndStepNameAndMaterialHash(String supplyChainId, String stepName, List<String> hashes);
    List<LinkMetaBlock> findByRunId(String supplyChainId, String runId);
}
