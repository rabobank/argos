package com.rabobank.argos.service.adapter.out.mongodb;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.service.domain.repository.LayoutMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LayoutMetaBlockRepositoryImpl implements LayoutMetaBlockRepository {

    private static final String COLLECTION = "layoutMetaBlocks";
    private static final String LAYOUT_ID_FIELD = "layoutMetaBlockId";
    private static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        createIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        createIndex(HashedIndex.hashed(LAYOUT_ID_FIELD));
    }

    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    @Override
    public void save(LayoutMetaBlock layoutMetaBlock) {
        template.save(layoutMetaBlock, COLLECTION);
    }

    @Override
    public Optional<LayoutMetaBlock> findBySupplyChainAndId(String supplyChainId, String layoutMetaBlockId) {
        Query query = getPrimaryQuery(supplyChainId, layoutMetaBlockId);
        return Optional.ofNullable(template.findOne(query, LayoutMetaBlock.class, COLLECTION));
    }

    private Query getPrimaryQuery(String supplyChainId, String layoutMetaBlockId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId).and(LAYOUT_ID_FIELD).is(layoutMetaBlockId));
    }

    @Override
    public List<LayoutMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query, LayoutMetaBlock.class, COLLECTION);
    }

    @Override
    public boolean update(String supplyChainId, String layoutMetaBlockId, LayoutMetaBlock layoutMetaBlock) {
        Query query = getPrimaryQuery(supplyChainId, layoutMetaBlockId);
        Document document = (Document) template.getConverter().convertToMongoType(layoutMetaBlock);
        UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), LayoutMetaBlock.class, COLLECTION);
        return updateResult.getMatchedCount() > 0;
    }
}
