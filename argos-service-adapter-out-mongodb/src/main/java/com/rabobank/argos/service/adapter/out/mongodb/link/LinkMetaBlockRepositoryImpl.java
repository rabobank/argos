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
package com.rabobank.argos.service.adapter.out.mongodb.link;

import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class LinkMetaBlockRepositoryImpl implements LinkMetaBlockRepository {

    static final String COLLECTION = "linkMetaBlocks";
    static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    static final String SEGMENT_NAME_FIELD = "link.layoutSegmentName";
    static final String STEP_NAME_FIELD = "link.stepName";
    static final String RUN_ID_FIELD = "link.runId";
    static final String LINK_MATERIALS_HASH_FIELD = "link.materials.hash";
    static final String LINK_PRODUCTS_HASH_FIELD = "link.products.hash";

    private final MongoTemplate template;

    @Override
    public List<LinkMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)
                .andOperator(
                        new Criteria()
                                .orOperator(
                                        new Criteria(LINK_MATERIALS_HASH_FIELD).is(hash),
                                        new Criteria(LINK_PRODUCTS_HASH_FIELD).is(hash)
                                )
                )
        );

        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public void save(LinkMetaBlock link) {
        template.save(link, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(String supplyChainId, String segmentName, String stepName, List<String> hashes) {
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        hashes.forEach(hash -> andCriteria.add(Criteria.where(LINK_PRODUCTS_HASH_FIELD).is(hash)));
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(String supplyChainId, String segmentName, String stepName, List<String> hashes) {
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        hashes.forEach(hash -> andCriteria.add(Criteria.where(LINK_MATERIALS_HASH_FIELD).is(hash)));
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
            String supplyChainId, String segmentName, String stepName, EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes) {
        if (artifactTypeHashes.isEmpty() || 
                (artifactTypeHashes.containsKey(ArtifactType.MATERIALS) && artifactTypeHashes.get(ArtifactType.MATERIALS).isEmpty()
                && artifactTypeHashes.containsKey(ArtifactType.PRODUCTS) && artifactTypeHashes.get(ArtifactType.PRODUCTS).isEmpty())) {
            List.of();
        }
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        if (artifactTypeHashes.containsKey(ArtifactType.MATERIALS)) {
            artifactTypeHashes.get(ArtifactType.MATERIALS).forEach(hash -> andCriteria.add(Criteria.where(LINK_MATERIALS_HASH_FIELD).is(hash)));
        }
        if (artifactTypeHashes.containsKey(ArtifactType.MATERIALS)) {
            artifactTypeHashes.get(ArtifactType.MATERIALS).forEach(hash -> andCriteria.add(Criteria.where(LINK_PRODUCTS_HASH_FIELD).is(hash)));
        }
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findByRunId(String supplyChainId, String runId) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)
                .andOperator(new Criteria(RUN_ID_FIELD).is(runId)));
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findByRunId(String supplyChainId, String segmentName, String runId, Set<String> resolvedSteps) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD)
                .is(supplyChainId)
                .and(RUN_ID_FIELD).is(runId)
                .and(SEGMENT_NAME_FIELD).is(segmentName)
                .and(STEP_NAME_FIELD).nin(resolvedSteps)
        );
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }
}
