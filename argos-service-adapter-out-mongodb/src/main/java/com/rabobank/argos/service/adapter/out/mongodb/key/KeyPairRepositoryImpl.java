package com.rabobank.argos.service.adapter.out.mongodb.key;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class KeyPairRepositoryImpl implements KeyPairRepository {

    private static final String COLLECTION = "keyPairs";
    private static final String KEY_ID = "keyId";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        createIndex(new Index(KEY_ID, Sort.Direction.ASC).unique());
    }

    @Override
    public void save(KeyPair keyPair) {
        template.save(keyPair, COLLECTION);
    }

    @Override
    public Optional<KeyPair> findByKeyId(String keyId) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(keyId), KeyPair.class, COLLECTION));
    }

    @Override
    public boolean exists(String keyId) {
        return template.exists(getPrimaryKeyQuery(keyId), KeyPair.class, COLLECTION);
    }

    private Query getPrimaryKeyQuery(String keyId) {
        return new Query(where(KEY_ID).is(keyId));
    }


    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }
}
