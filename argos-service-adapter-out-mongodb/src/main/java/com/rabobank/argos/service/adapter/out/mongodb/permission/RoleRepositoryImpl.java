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
package com.rabobank.argos.service.adapter.out.mongodb.permission;

import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Component
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    static final String COLLECTION = "roles";
    static final String ROLE_ID_FIELD = "roleId";
    static final String ROLE_NAME_FIELD = "name";
    private final MongoTemplate template;

    @Override
    public void save(Role role) {
        template.save(role, COLLECTION);
    }

    @Override
    public List<Role> findAll() {
        return template.findAll(Role.class, COLLECTION);
    }

    @Override
    public List<Role> findByIds(List<String> roleIds) {
        Query query = new Query(Criteria.where(ROLE_ID_FIELD).in(roleIds));
        query.with(Sort.by(ASC, "name"));
        return template.find(query, Role.class, COLLECTION);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(template.findOne(new Query(Criteria.where(ROLE_NAME_FIELD).is(name)), Role.class, COLLECTION));
    }

}
