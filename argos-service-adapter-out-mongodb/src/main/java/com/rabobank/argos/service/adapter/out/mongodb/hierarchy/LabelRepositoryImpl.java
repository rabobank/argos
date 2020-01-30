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
package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LabelRepositoryImpl implements LabelRepository {

    private static final String COLLECTION = "supplyChainlabels";
    private static final String SUPPLY_CHAIN_LABEL_ID_FIELD = "id";
    private static final String SUPPLY_CHAIN_LABEL_NAME = "name";
    private final MongoTemplate template;

    @Override
    public boolean exists(String id) {
        return template.exists(getPrimaryKeyQuery(id), Label.class, COLLECTION);
    }

    @Override
    public void save(Label label) {
        template.save(label, COLLECTION);
    }

    @Override
    public Optional<Label> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), Label.class, COLLECTION));
    }

    @Override
    public Optional<Label> findByNameAndPathToRoot(String name, List<String> pathToRoot) {

        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_LABEL_NAME)
                .is(name)
                .andOperator(Criteria.where("pathToRoot").is(pathToRoot));
        Query query = new Query(rootCriteria);
        return Optional.of(
                template.findOne(query, Label.class, COLLECTION));

    }

    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_LABEL_ID_FIELD).is(supplyChainId));
    }

}
