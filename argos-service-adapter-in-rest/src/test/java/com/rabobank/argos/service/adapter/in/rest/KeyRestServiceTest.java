package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.KeyPairRepository;
import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.mapper.KeyPairMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyRestServiceTest {
    private static final String KEY_ID = "keyId";
    @Mock
    private KeyPairMapper converter;
    @Mock
    private KeyPairRepository keyPairRepository;
    @Mock
    RestKeyPair restKeyPair;
    @Mock
    KeyPair keyPair;

    private KeyRestService restService;

    @BeforeEach
    void setUp() {
        restService = new KeyRestService(converter, keyPairRepository);
    }

    @Test
    void getKeyShouldReturnSuccess() {
        when(converter.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(java.util.Optional.of(keyPair));
        assertThat(restService.getKey(KEY_ID).getStatusCodeValue(), is(200));
        verify(keyPairRepository).findByKeyId(KEY_ID);
        verify(converter).convertToRestKeyPair(keyPair);
    }

    @Test
    void storeKeyShouldreturnSuccess() {
        when(converter.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        assertThat(restService.storeKey(restKeyPair).getStatusCodeValue(), is(204));
        verify(keyPairRepository).save(keyPair);
    }

    @Test
    void getKeyShouldReturnNotFound() {
        assertThrows(ResponseStatusException.class, () -> {
            restService.getKey(KEY_ID);
        });
        verify(keyPairRepository).findByKeyId(KEY_ID);
    }
}