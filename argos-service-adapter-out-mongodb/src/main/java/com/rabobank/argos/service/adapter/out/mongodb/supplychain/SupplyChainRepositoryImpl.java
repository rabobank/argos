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

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainRepositoryImpl implements SupplyChainRepository {

    static final String COLLECTION = "supplyChains";
    static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    static final String SUPPLY_CHAIN_NAME_FIELD = "name";
    static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;

    @Override
    public void save(SupplyChain supplyChain) {
        try {
            template.save(supplyChain, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(supplyChain, e);
        }
    }

    @Override
    public Optional<SupplyChain> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION));
    }

    @Override
    public Optional<SupplyChain> update(String supplyChainId, SupplyChain supplyChain) {
        Query query = getPrimaryKeyQuery(supplyChainId);
        Document document = new Document();
        template.getConverter().write(supplyChain, document);
        try {
            UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), SupplyChain.class, COLLECTION);
            if (updateResult.getMatchedCount() > 0) {
                supplyChain.setSupplyChainId(supplyChainId);
                return Optional.of(supplyChain);
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(supplyChain, e);
        }
    }

    @Override
    public boolean exists(String supplyChainId) {
        return template.exists(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION);
    }

    @Override
    public Optional<String> findParentLabelIdBySupplyChainId(String supplyChainId) {
        Query query = getPrimaryKeyQuery(supplyChainId);
        query.fields().include(PARENT_LABEL_ID_FIELD);
        return Optional.ofNullable(template.findOne(query, String.class, COLLECTION));
    }

    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }

    private ArgosError duplicateKeyException(SupplyChain supplyChain, DuplicateKeyException e) {
        return new ArgosError("supply chain with name: " + supplyChain.getName() + " and parentLabelId: " + supplyChain.getParentLabelId() + " already exists", e, ArgosError.Level.WARNING);
    }
}
