package com.rabobank.argos.service.domain.supplychain;

import com.rabobank.argos.domain.supplychain.SupplyChain;

import java.util.List;
import java.util.Optional;

public interface SupplyChainRepository {
    Optional<SupplyChain> findBySupplyChainId(String supplyChainId);

    boolean exists(String supplyChainId);
    List<SupplyChain> findByName(String name);
    List<SupplyChain> findAll();
    void save(SupplyChain supplyChain);
}
