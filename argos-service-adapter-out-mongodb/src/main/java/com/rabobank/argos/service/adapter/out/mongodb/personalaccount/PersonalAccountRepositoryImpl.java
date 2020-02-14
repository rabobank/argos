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
package com.rabobank.argos.service.adapter.out.mongodb.personalaccount;

import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.domain.account.Account;
import com.rabobank.argos.service.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class PersonalAccountRepositoryImpl implements PersonalAccountRepository {

    static final String COLLECTION = "personalaccounts";
    static final String ACCOUNT_ID = "accountId";
    static final String EMAIL = "email";
    private final MongoTemplate template;

    @Override
    public Optional<PersonalAccount> findByEmail(String email) {
        return Optional.ofNullable(template.findOne(new Query(where(EMAIL).is(email)), PersonalAccount.class, COLLECTION));
    }

    @Override
    public Optional<PersonalAccount> findByAccountId(String accountId) {
        return Optional.ofNullable(template.findOne(getPrimaryQuery(accountId), PersonalAccount.class, COLLECTION));
    }

    @Override
    public void save(PersonalAccount personalAccount) {
        template.save(personalAccount, COLLECTION);
    }

    @Override
    public void update(PersonalAccount existingPersonalAccount) {
        Query query = getPrimaryQuery(existingPersonalAccount.getAccountId());
        Document document = new Document();
        template.getConverter().write(existingPersonalAccount, document);
        template.updateFirst(query, Update.fromDocument(document), PersonalAccount.class, COLLECTION);
    }

    @Override
    public Optional<KeyPair> findActiveKeyPair(String accountId) {
        return findByAccountId(accountId).map(Account::getActiveKeyPair);
    }

    private Query getPrimaryQuery(String userId) {
        return new Query(where(ACCOUNT_ID).is(userId));
    }
}
