package com.rabobank.argos.service.adapter.out.mongodb.supplychain;

import com.rabobank.argos.domain.supplychain.SupplyChainLabel;
import com.rabobank.argos.service.domain.supplychain.SupplyChainLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainLabelRepositoryImpl implements SupplyChainLabelRepository {

    private static final String COLLECTION = "supplyChainlabels";
    private static final String SUPPLY_CHAIN_LABEL_ID_FIELD = "id";
    private static final String SUPPLY_CHAIN_LABEL_NAME = "name";
    private final MongoTemplate template;

    @Override
    public boolean exists(String id) {
        return template.exists(getPrimaryKeyQuery(id), SupplyChainLabel.class, COLLECTION);
    }

    @Override
    public void save(SupplyChainLabel supplyChainLabel) {
        template.save(supplyChainLabel, COLLECTION);
    }

    @Override
    public Optional<SupplyChainLabel> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), SupplyChainLabel.class, COLLECTION));
    }

    @Override
    public Optional<SupplyChainLabel> findByNameAndPathToRoot(String name, List<String> pathToRoot) {
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_LABEL_NAME)
                .is(name)
                .andOperator(Criteria.where("pathToRoot").is(pathToRoot));
        Query query = new Query(rootCriteria);
        return Optional.of(
                template.findOne(query, SupplyChainLabel.class, COLLECTION));

    }

    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_LABEL_ID_FIELD).is(supplyChainId));
    }

}
