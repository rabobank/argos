package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.RepositoryResetProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Profile("integration-test")
@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryResetProviderImpl implements RepositoryResetProvider {

    private final MongoTemplate template;

    @Override
    public void resetAllRepositories() {
        template.getCollectionNames().forEach(c-> template.dropCollection(c));
    }
}
