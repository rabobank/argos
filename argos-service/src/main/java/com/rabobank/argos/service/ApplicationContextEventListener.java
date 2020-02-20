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
package com.rabobank.argos.service;

import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.permission.GlobalPermission;
import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
@Slf4j
public class ApplicationContextEventListener {
    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Application context started {}", contextRefreshedEvent);
        PersonalAccountRepository personalAccountRepository = contextRefreshedEvent
                .getApplicationContext()
                .getBean(PersonalAccountRepository.class);

        RoleRepository roleRepository = contextRefreshedEvent
                .getApplicationContext()
                .getBean(RoleRepository.class);

        long numberOfUsers = personalAccountRepository.findAll().size();
        Role role;
        if (numberOfUsers == 1) {
            log.info("Found one user ");
            if (roleRepository.findAll().isEmpty()) {
                role = Role.builder()
                        .name("administrator")
                        .permissions(asList(GlobalPermission.values()))
                        .build();
                roleRepository.save(role);
            } else {
                role = roleRepository.findAll().iterator().next();
            }
            PersonalAccount personalAccount = personalAccountRepository.findAll().iterator().next();
            if (personalAccount.getRoleIds().isEmpty()) {
                personalAccount.setRoleIds(singletonList(role)
                        .stream()
                        .map(Role::getRoleId)
                        .collect(Collectors.toList()));
                personalAccountRepository.update(personalAccount);
                log.info("Assigned administrator role to personal account ");
            }

        }
    }

    @EventListener
    public void handleContextClosedEvent(ContextClosedEvent contextClosedEvent) {
        log.info("Application context stopped {}", contextClosedEvent);
    }
}
