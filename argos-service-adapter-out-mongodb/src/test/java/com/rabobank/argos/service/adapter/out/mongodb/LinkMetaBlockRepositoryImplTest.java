package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkMetaBlockRepositoryImplTest {


    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SHA = "sha";
    private static String COLLECTION_NAME = "linkMetaBlocks";

    @Mock
    private MongoTemplate template;

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private LinkMetaBlock link;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private LinkMetaBlockRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LinkMetaBlockRepositoryImpl(template);
    }

    @Test
    void postPostConstruct() {
        when(template.indexOps(COLLECTION_NAME)).thenReturn(indexOperations);
        repository.postConstruct();
        verify(template, times(4)).indexOps(COLLECTION_NAME);
    }

    @Test
    void findBySupplyChainId() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndSha() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSha(SUPPLY_CHAIN_ID, SHA);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"$or\" : [{ \"link.materials.hash\" : \"sha\"}, { \"link.products.hash\" : \"sha\"}]}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHash() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndStepNameAndProductHash(SUPPLY_CHAIN_ID, "stepName", SHA);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION_NAME));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"stepName\" : \"stepName\"}, { \"link.products.hash\" : \"sha\"}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(link);
        verify(template).save(link, COLLECTION_NAME);
    }
}