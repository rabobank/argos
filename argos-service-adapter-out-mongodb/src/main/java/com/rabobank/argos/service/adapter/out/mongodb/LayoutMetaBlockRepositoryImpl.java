package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.repository.LayoutMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LayoutMetaBlockRepositoryImpl implements LayoutMetaBlockRepository {

    private static final String COLLECTION = "layoutMetaBlocks";
    private static final String LAYOUT_ID_FIELD = "id";
    private final MongoTemplate template;

    @Override
    public void save(LayoutMetaBlock layoutMetaBlock) {
        template.save(layoutMetaBlock, COLLECTION);
    }

    @Override
    public Optional<LayoutMetaBlock> findById(String id) {
        Query query = new Query(Criteria.where(LAYOUT_ID_FIELD).is(id));
        return Optional.ofNullable(template.findOne(query, LayoutMetaBlock.class, COLLECTION));
    }
}
