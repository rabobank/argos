package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.service.domain.RepositoryResetProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Profile("integration-test")
@Component
@RequiredArgsConstructor
public class RepositoryResetProviderImpl implements RepositoryResetProvider {

    private final MongoTemplate template;

    @Override
    public void resetAllRepositories() {
        template.getCollectionNames().forEach(template::dropCollection);
    }
}
