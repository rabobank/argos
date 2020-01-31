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

import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainRepositoryImpl implements SupplyChainRepository {

    static final String COLLECTION = "supplyChains";
    static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    static final String SUPPLY_CHAIN_NAME = "name";
    private final MongoTemplate template;

    @Override
    public Optional<SupplyChain> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION));
    }

    @Override
    public boolean exists(String supplyChainId) {
        return template.exists(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION);
    }

    @Override
    public List<SupplyChain> findByName(String name) {
        Query query = new Query(Criteria.where(SUPPLY_CHAIN_NAME).is(name));
        return template.find(query, SupplyChain.class, COLLECTION);
    }

    @Override
    public Optional<SupplyChain> findByNameAndPathToRoot(String name, List<String> pathToRoot) {
        return Optional.empty();
    }

    @Override
    public List<SupplyChain> findAll() {
        return template.findAll(SupplyChain.class, COLLECTION);
    }

    @Override
    public void save(SupplyChain supplyChain) {
        template.save(supplyChain, COLLECTION);
    }


    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }

}
