package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.domain.KeyPairRepository;
import com.rabobank.argos.domain.model.KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyPairRepositoryImplTest {
    private static final String COLLECTION = "keyPair";
    @Mock
    private MongoTemplate template;

    private KeyPairRepository keyPairRepository;

    @Mock
    private KeyPair keyPair;

    @BeforeEach
    void setUp() {
        keyPairRepository = new KeyPairRepositoryImpl(template);
    }

    @Test
    void save() {
        keyPairRepository.save(keyPair);
        verify(template).save(keyPairRepository, COLLECTION);
    }

    @Test
    void findByKeyId() {
        when(template.findOne(any(), eq(KeyPair.class), eq(COLLECTION))).thenReturn(keyPair);
    }
}