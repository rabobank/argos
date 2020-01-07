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
package com.rabobank.argos.service.adapter.out.mongodb.layout;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LayoutMetaBlockRepositoryImpl implements LayoutMetaBlockRepository {

    private static final String COLLECTION = "layoutMetaBlocks";
    private static final String LAYOUT_ID_FIELD = "layoutMetaBlockId";
    private static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        createIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        createIndex(HashedIndex.hashed(LAYOUT_ID_FIELD));
    }

    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    @Override
    public void save(LayoutMetaBlock layoutMetaBlock) {
        template.save(layoutMetaBlock, COLLECTION);
    }

    @Override
    public Optional<LayoutMetaBlock> findBySupplyChainAndId(String supplyChainId, String layoutMetaBlockId) {
        Query query = getPrimaryQuery(supplyChainId, layoutMetaBlockId);
        return Optional.ofNullable(template.findOne(query, LayoutMetaBlock.class, COLLECTION));
    }

    private Query getPrimaryQuery(String supplyChainId, String layoutMetaBlockId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId).and(LAYOUT_ID_FIELD).is(layoutMetaBlockId));
    }

    @Override
    public List<LayoutMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query, LayoutMetaBlock.class, COLLECTION);
    }

    @Override
    public boolean update(String supplyChainId, String layoutMetaBlockId, LayoutMetaBlock layoutMetaBlock) {
        Query query = getPrimaryQuery(supplyChainId, layoutMetaBlockId);
        Document document = new Document();
        template.getConverter().write(layoutMetaBlock, document);
        UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), LayoutMetaBlock.class, COLLECTION);
        return updateResult.getMatchedCount() > 0;
    }
}
