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

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LabelRepositoryImpl implements LabelRepository {

    static final String COLLECTION = "labels";
    static final String LABEL_ID_FIELD = "labelId";
    static final String LABEL_NAME_FIELD = "name";
    static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;


    @Override
    public void save(Label label) {
        template.save(label, COLLECTION);
    }

    @Override
    public Optional<Label> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), Label.class, COLLECTION));
    }

    @Override
    public boolean deleteById(String id) {
        // delete also all sub labels
        return false;
    }

    @Override
    public Optional<Label> update(String id, Label label) {
        Query query = getPrimaryKeyQuery(id);
        Document document = new Document();
        template.getConverter().write(label, document);
        UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), Label.class, COLLECTION);
        if (updateResult.getMatchedCount() > 0) {
            label.setLabelId(id);
            return Optional.of(label);
        } else {
            return Optional.empty();
        }
    }

    private Query getPrimaryKeyQuery(String id) {
        return new Query(Criteria.where(LABEL_ID_FIELD).is(id));
    }

}
