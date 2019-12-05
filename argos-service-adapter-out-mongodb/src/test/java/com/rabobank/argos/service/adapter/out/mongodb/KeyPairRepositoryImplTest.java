package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.out.mongodb.key.KeyPairRepositoryImpl;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyPairRepositoryImplTest {
    private static final String COLLECTION = "keyPairs";
    @Mock
    private MongoTemplate template;

    private KeyPairRepositoryImpl keyPairRepository;

    @Mock
    private KeyPair keyPair;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Mock
    private IndexOperations indexOperations;

    @BeforeEach
    void setUp() {
        keyPairRepository = new KeyPairRepositoryImpl(template);
    }

    @Test
    void postConstructShouldConfigure() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        keyPairRepository.postConstruct();
        verify(template, times(1)).indexOps(COLLECTION);
    }

    @Test
    void saveShouldStoreKeyPair() {
        keyPairRepository.save(keyPair);
        verify(template).save(keyPair, COLLECTION);
    }

    @Test
    void findByKeyIdShouldFindKeypair() {
        when(template.findOne(any(), eq(KeyPair.class), eq(COLLECTION))).thenReturn(keyPair);
        keyPairRepository.findByKeyId("keyId");
        verify(template).findOne(queryArgumentCaptor.capture(), eq(KeyPair.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"keyId\" : \"keyId\"}, Fields: {}, Sort: {}"));
    }
}
