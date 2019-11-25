package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.domain.repository.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainRepositoryImpl implements SupplyChainRepository {

    private static final String COLLECTION = "supplyChains";
    private static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private static final String SUPPLY_CHAIN_NAME = "name";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        template.indexOps(COLLECTION).ensureIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        template.indexOps(COLLECTION).ensureIndex(new Index().on(SUPPLY_CHAIN_NAME, Sort.Direction.ASC));
    }

    @Override
    public Optional<SupplyChain> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION));
    }

    @Override
    public boolean exists(String supplyChainId) {
        return template.exists(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION);
    }

    @Override
    public List<SupplyChain> findByName(String name) {
        Query query = new Query(Criteria.where(SUPPLY_CHAIN_NAME).is(name));
        return template.find(query, SupplyChain.class, COLLECTION);
    }

    @Override
    public List<SupplyChain> findAll() {
        return template.findAll(SupplyChain.class, COLLECTION);
    }

    @Override
    public void save(SupplyChain supplyChain) {
        template.save(supplyChain, COLLECTION);
    }


    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }

}
