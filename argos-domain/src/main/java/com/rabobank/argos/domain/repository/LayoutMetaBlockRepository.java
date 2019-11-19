package com.rabobank.argos.domain.repository;

import com.rabobank.argos.domain.model.LayoutMetaBlock;

import java.util.Optional;

public interface LayoutMetaBlockRepository {
    void save(LayoutMetaBlock layoutMetaBlock);

    Optional<LayoutMetaBlock> findById(String id);
}
