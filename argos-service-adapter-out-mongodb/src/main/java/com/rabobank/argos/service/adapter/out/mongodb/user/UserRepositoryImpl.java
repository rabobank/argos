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
package com.rabobank.argos.service.adapter.out.mongodb.user;

import com.rabobank.argos.service.domain.user.User;
import com.rabobank.argos.service.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private static final String COLLECTION = "users";
    private static final String USER_ID = "userId";
    private static final String EMAIL = "email";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        createIndex(new Index(USER_ID, Sort.Direction.ASC).unique());
        createIndex(new Index(EMAIL, Sort.Direction.ASC).unique());
    }

    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(template.findOne(new Query(where(EMAIL).is(email)), User.class, COLLECTION));
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return Optional.ofNullable(template.findOne(getPrimaryQuery(userId), User.class, COLLECTION));
    }

    @Override
    public User save(User user) {
        template.save(user, COLLECTION);
        return user;
    }

    @Override
    public User update(User existingUser) {
        Query query = getPrimaryQuery(existingUser.getUserId());
        Document document = new Document();
        template.getConverter().write(existingUser, document);
        template.updateFirst(query, Update.fromDocument(document), User.class, COLLECTION);
        return existingUser;
    }

    private Query getPrimaryQuery(String userId) {
        return new Query(where(USER_ID).is(userId));
    }
}
