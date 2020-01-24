package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SupplyChainNodeUpDater implements HierarchicalNodeVisitor<SupplyChainNodeUpDater.UpdateResults> {
    private UpdateResults updateResults;

    @Override
    public boolean visitEnter(Label label) {
        log.info("entered node: {} {} {} {} ", label.getName(), label.getLft(), label.getRght(), label.getDepth());

        return true;
    }

    @Override
    public boolean visitExit(Label label) {
        log.info("exited node: {} {} {} {} ", label.getName(), label.getLft(), label.getRght(), label.getDepth());
        return true;
    }

    @Override
    public boolean visitLeaf(SupplyChain supplyChain) {
        log.info("entered leaf: {}", supplyChain.getName());
        return true;
    }

    @Override
    public UpdateResults result() {
        return updateResults;
    }

    @Data
    public static class UpdateResults {
        List<SupplyChainNode> updated;
        List<SupplyChainNode> created;
    }
}
