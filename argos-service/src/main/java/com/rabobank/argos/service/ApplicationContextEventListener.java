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
            if (roleRepository.findAll().size() == 0) {
                role = Role.builder()
                        .name("administrator")
                        .permissions(asList(GlobalPermission.values()))
                        .build();
                roleRepository.save(role);
            } else {
                role = roleRepository.findAll().iterator().next();
            }
            PersonalAccount personalAccount = personalAccountRepository.findAll().iterator().next();
            if (personalAccount.getRoleIds().size() == 0) {
                personalAccount.setRoleIds(singletonList(role)
                        .stream()
                        .map(r -> r.getRoleId())
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
