package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.model.SupplyChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRepositoryImplTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    public static final String COLLECTION_NAME = "supplyChains";
    @Mock
    private MongoTemplate template;

    @Mock
    private IndexOperations indexOperations;

    @Captor
    private ArgumentCaptor<IndexDefinition> indexDefinitionArgumentCaptor;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private SupplyChainRepositoryImpl repository;

    @Mock
    private SupplyChain supplyChain;

    @BeforeEach
    void setUp() {
        repository = new SupplyChainRepositoryImpl(template);
    }

    @Test
    void postPostConstruct() {
        when(template.indexOps(COLLECTION_NAME)).thenReturn(indexOperations);
        repository.postConstruct();
        verify(indexOperations).ensureIndex(indexDefinitionArgumentCaptor.capture());
        assertThat(indexDefinitionArgumentCaptor.getValue().getIndexKeys().toJson(), is("{\"supplyChainId\": \"hashed\"}"));
    }

    @Test
    void findBySupplyChainId() {
        when(template.findOne(any(), eq(SupplyChain.class), eq(COLLECTION_NAME))).thenReturn(supplyChain);
        assertThat(repository.findBySupplyChainId(SUPPLY_CHAIN_ID), is(Optional.of(supplyChain)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION_NAME));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(supplyChain);
        verify(template).save(supplyChain, COLLECTION_NAME);
    }
}