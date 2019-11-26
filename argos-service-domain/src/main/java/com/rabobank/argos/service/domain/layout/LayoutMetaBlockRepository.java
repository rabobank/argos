package com.rabobank.argos.service.domain.layout;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;

import java.util.List;
import java.util.Optional;

public interface LayoutMetaBlockRepository {
    void save(LayoutMetaBlock layoutMetaBlock);

    Optional<LayoutMetaBlock> findBySupplyChainAndId(String supplyChainId, String id);

    List<LayoutMetaBlock> findBySupplyChainId(String supplyChainId);

    boolean update(String supplyChainId, String layoutMetaBlockId, LayoutMetaBlock layoutMetaBlock);
}
