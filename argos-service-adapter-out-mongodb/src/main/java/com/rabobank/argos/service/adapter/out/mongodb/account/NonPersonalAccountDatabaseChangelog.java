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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.ACCOUNT_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.ACCOUNT_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.PARENT_LABEL_ID_FIELD;
import static org.springframework.data.domain.Sort.Direction.ASC;

@ChangeLog
public class NonPersonalAccountDatabaseChangelog {

    @ChangeSet(order = "001", id = "NonPersonalAccountDatabaseChangelog-1", author = "bart")
    public void addIndex(MongoTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(ACCOUNT_ID_FIELD, ASC).unique());
        template.indexOps(COLLECTION)
                .ensureIndex(new CompoundIndexDefinition(new Document(Map.of(PARENT_LABEL_ID_FIELD, 1, ACCOUNT_NAME_FIELD, 1)))
                        .named(PARENT_LABEL_ID_FIELD + "_" + ACCOUNT_NAME_FIELD).unique());
    }

    @ChangeSet(order = "002", id = "NonPersonalAccountDatabaseChangelog-2", author = "bart")
    public void addActiveKeyIndex(MongoTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(ACTIVE_KEY_ID_FIELD, ASC)
                .partial(PartialIndexFilter.of(new Criteria(ACTIVE_KEY_ID_FIELD).exists(true))).unique());
    }
}