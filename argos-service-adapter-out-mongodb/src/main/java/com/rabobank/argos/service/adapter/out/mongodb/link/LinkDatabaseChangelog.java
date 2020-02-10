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

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.LINK_MATERIALS_HASH_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.LINK_PRODUCTS_HASH_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.SEGMENT_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.STEP_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD;


@ChangeLog
public class LinkDatabaseChangelog {

    @ChangeSet(order = "001", id = "LinkDatabaseChangelog-1", author = "bart")
    public void addIndexes(MongoTemplate template) {
        createIndex(template, HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        createIndex(template, new CompoundIndexDefinition(new Document(LINK_MATERIALS_HASH_FIELD, 1)).named(LINK_MATERIALS_HASH_FIELD));
        createIndex(template, new CompoundIndexDefinition(new Document(LINK_PRODUCTS_HASH_FIELD, 1)).named(LINK_PRODUCTS_HASH_FIELD));
        createCompoundIndexOnSupplyChainAndStepName(template);
    }

    private void createCompoundIndexOnSupplyChainAndStepName(MongoTemplate template) {
        createIndex(template, new CompoundIndexDefinition(new Document(Map.of(SUPPLY_CHAIN_ID_FIELD, 1, SEGMENT_NAME_FIELD, 1, STEP_NAME_FIELD, 1)))
                .named(SUPPLY_CHAIN_ID_FIELD + "_" + SEGMENT_NAME_FIELD + "_" + STEP_NAME_FIELD));
    }

    private void createIndex(MongoTemplate template, IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

}