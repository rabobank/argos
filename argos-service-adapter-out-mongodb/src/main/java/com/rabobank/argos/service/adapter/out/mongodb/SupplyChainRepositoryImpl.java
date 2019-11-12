package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.SupplyChainRepository;
import com.rabobank.argos.domain.model.SupplyChain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainRepositoryImpl implements SupplyChainRepository {

    private static final String COLLECTION = "supplyChains";
    public static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        template.indexOps(COLLECTION).ensureIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
    }

    @Override
    public Optional<SupplyChain> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return Optional.ofNullable(template.findOne(query, SupplyChain.class, COLLECTION));
    }

    @Override
    public void save(SupplyChain supplyChain) {
        template.save(supplyChain, COLLECTION);
    }
}
