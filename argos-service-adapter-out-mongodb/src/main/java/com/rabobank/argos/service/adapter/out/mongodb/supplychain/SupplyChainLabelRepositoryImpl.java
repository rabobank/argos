package com.rabobank.argos.service.adapter.out.mongodb.supplychain;

import com.rabobank.argos.domain.supplychain.SupplyChainLabel;
import com.rabobank.argos.service.domain.supplychain.SupplyChainLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class SupplyChainLabelRepositoryImpl implements SupplyChainLabelRepository {

    private static final String COLLECTION = "supplyChainlabels";
    private static final String SUPPLY_CHAIN_LABEL_ID_FIELD = "id";
    private static final String SUPPLY_CHAIN_LABEL_NAME = "name";
    private final MongoTemplate template;

    @Override
    public void save(SupplyChainLabel supplyChainLabel) {


    }

    @Override
    public SupplyChainLabel findById(String id) {
        return null;
    }

    @Override
    public SupplyChainLabel findByNameAndPathToRoot(String name, List<String> pathToRoot) {
        return null;
    }
}
