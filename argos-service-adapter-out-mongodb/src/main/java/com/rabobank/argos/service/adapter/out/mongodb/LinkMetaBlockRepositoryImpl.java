package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.service.domain.repository.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class LinkMetaBlockRepositoryImpl implements LinkMetaBlockRepository {

    private static final String COLLECTION = "linkMetaBlocks";
    private static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    public static final String LINK_MATERIALS_HASH_FIELD = "link.materials.hash";
    public static final String LINK_PRODUCTS_HASH_FIELD = "link.products.hash";

    private final MongoTemplate template;

    @PostConstruct
    public void postConstruct() {
        createIndex(HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        createIndex(new CompoundIndexDefinition(new Document(LINK_MATERIALS_HASH_FIELD,1)).named(LINK_MATERIALS_HASH_FIELD));
        createIndex(new CompoundIndexDefinition(new Document(LINK_PRODUCTS_HASH_FIELD,1)).named(LINK_PRODUCTS_HASH_FIELD));
    }

    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId).andOperator(
                new Criteria().orOperator(new Criteria(LINK_MATERIALS_HASH_FIELD).is(hash),new Criteria(LINK_PRODUCTS_HASH_FIELD).is(hash))));
        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public void save(LinkMetaBlock link) {
        template.save(link, COLLECTION);
    }
}
