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
package com.rabobank.argos.service.adapter.out.mongodb.supplychain;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.PARENT_LABEL_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.SUPPLY_CHAIN_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.SUPPLY_CHAIN_NAME_FIELD;


@ChangeLog
public class SupplyChainDatabaseChangelog {

    @ChangeSet(order = "001", id = "SupplyChainDatabaseChangelog-1", author = "bart")
    public void addIndex(MongoTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        template.indexOps(COLLECTION)
                .ensureIndex(new CompoundIndexDefinition(new Document(Map.of(PARENT_LABEL_ID_FIELD, 1, SUPPLY_CHAIN_NAME_FIELD, 1)))
                        .named(PARENT_LABEL_ID_FIELD + "_" + SUPPLY_CHAIN_NAME_FIELD).unique());
    }

}