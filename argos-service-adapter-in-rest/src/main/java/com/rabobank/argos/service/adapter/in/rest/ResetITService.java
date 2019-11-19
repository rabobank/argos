package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.RepositoryResetProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("integration-test")
@RequestMapping("/integration-test")
@RestController
@RequiredArgsConstructor
public class ResetITService {

    private final RepositoryResetProvider repositoryResetProvider;

    @PostMapping(value = "/reset-db")
    public void resetdb() {
        repositoryResetProvider.resetAllRepositories();
    }


}
