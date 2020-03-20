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
package com.rabobank.argos.integrationtest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RepositoryResetProviderImpl implements RepositoryResetProvider {

    private final MongoTemplate template;

    private static final Set<String> IGNORED_COLLECTIONS_FOR_ALL = Set.of("dbchangelog", "mongobeelock", "hierarchy", "hierarchy_tmp", "system.views", "roles");
    private static final Set<String> IGNORED_COLLECTIONS = new HashSet<>();

    static {
        IGNORED_COLLECTIONS.addAll(IGNORED_COLLECTIONS_FOR_ALL);
        IGNORED_COLLECTIONS.add("personalaccounts");
        IGNORED_COLLECTIONS.add("labels");
        IGNORED_COLLECTIONS.add("nonPersonalAccounts");
    }

    @Override
    public void resetAllRepositories() {
        template.getCollectionNames().stream()
                .filter(name -> !IGNORED_COLLECTIONS_FOR_ALL.contains(name))
                .forEach(name -> template.remove(new Query(), name));
    }

    @Override
    public void resetNotAllRepositories() {
        template.getCollectionNames().stream()
                .filter(name -> !IGNORED_COLLECTIONS.contains(name))
                .forEach(name -> template.remove(new Query(), name));
        template.remove(new Query(Criteria.where("email").nin("luke@skywalker.imp", "default@nl.nl")), "personalaccounts");
        template.remove(new Query(Criteria.where("name").nin("default_root_label")), "labels");
        template.remove(new Query(Criteria.where("name").nin("default-npa1", "default-npa2", "default-npa3", "default-npa4", "default-npa5")), "nonPersonalAccounts");
    }

    @Override
    public void deletePersonalAccount(String accountId) {
        template.remove(new Query(Criteria.where("accountId").is(accountId)), "personalaccounts");
    }


}
