package com.rabobank.argos.service.domain.repository;

import com.rabobank.argos.domain.model.KeyPair;

import java.util.Optional;

public interface KeyPairRepository {
    void save(KeyPair keyPair);

    Optional<KeyPair> findByKeyId(String keyId);

    boolean exists(String keyId);
}
