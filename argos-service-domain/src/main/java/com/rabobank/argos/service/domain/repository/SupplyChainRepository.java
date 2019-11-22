package com.rabobank.argos.service.domain.repository;

import com.rabobank.argos.domain.model.SupplyChain;

import java.util.List;
import java.util.Optional;

public interface SupplyChainRepository {
    Optional<SupplyChain> findBySupplyChainId(String supplyChainId);
    List<SupplyChain> findByName(String name);
    List<SupplyChain> findAll();
    void save(SupplyChain supplyChain);
}
