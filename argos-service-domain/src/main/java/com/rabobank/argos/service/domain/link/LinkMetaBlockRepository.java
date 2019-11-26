package com.rabobank.argos.service.domain.link;


import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.List;

public interface LinkMetaBlockRepository {
    List<LinkMetaBlock> findBySupplyChainId(String supplyChainId);
    List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash);
    void save(LinkMetaBlock link);

    List<LinkMetaBlock> findBySupplyChainStepNameAndSha(String supplyChainId, String stepName, String hash);

    List<LinkMetaBlock> findByRunId(String supplyChainId, String runId);

}
