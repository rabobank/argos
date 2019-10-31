package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.LinkMetaBlockRepository;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LinkMetaBlockMetaBlockRepositoryImpl implements LinkMetaBlockRepository {

    private static final String COLLECTION = "linkMetaBlocks";

    private final MongoTemplate template;

    @PostConstruct
    public void postPostConstruct() {
        createIndex(HashedIndex.hashed("supplyChainId"));
        createIndex(new CompoundIndexDefinition(new Document("link.materials.hash",1)).named("link.materials.hash"));
        createIndex(new CompoundIndexDefinition(new Document("link.products.hash",1)).named("link.products.hash"));
    }

    private void createIndex(IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(where("supplyChainId").is(supplyChainId));
        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash) {
        Query query = new Query(new Criteria("supplyChainId").is(supplyChainId).andOperator(
                new Criteria().orOperator(new Criteria("link.materials.hash").is(hash),new Criteria("link.products.hash").is(hash))));
        log.info("{}", query);
        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public void save(LinkMetaBlock link) {
        template.save(link, COLLECTION);
    }
}
