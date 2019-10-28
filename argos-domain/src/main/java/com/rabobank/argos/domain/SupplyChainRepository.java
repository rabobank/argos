package com.rabobank.argos.domain;

import com.rabobank.argos.domain.model.SupplyChain;

import java.util.Optional;

public interface SupplyChainRepository {
    Optional<SupplyChain> findBySupplyChainId(String supplyChainId);

    void save(SupplyChain supplyChain);
}
