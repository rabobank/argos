package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Builder
@Slf4j
public class SupplyChainNodeInserter implements HierarchicalNodeVisitor<Boolean> {
    private SupplyChainNode nodeToInsert;
    private String parentRef;
    private Boolean result;

    @Override
    public boolean visitEnter(Label label) {
        log.info("entered node: {} {} {} {} ", label.getName(), label.getLft(), label.getRght(), label.getDepth());
        if (label.getChildren() != null) {
            Optional<Label> parentNode = label.getChildren()
                    .stream()
                    .filter(lnode -> lnode.hasChildren() && lnode.getId().equals(parentRef))
                    .map(node -> (Label) node)
                    .findFirst();
            parentNode.ifPresent((foundNode -> {
                foundNode.addChild(nodeToInsert);
                log.info("child inserted into {}", foundNode.getName());
            }));
            result = parentNode.isPresent();
            return parentNode.isEmpty();
        }

        return false;
    }

    @Override
    public boolean visitExit(Label label) {
        log.info("exited node: {} {} {} {} ", label.getName(), label.getLft(), label.getRght(), label.getDepth());
        return true;
    }

    @Override
    public boolean visitLeaf(SupplyChain supplyChain) {
        return false;
    }

    @Override
    public Boolean result() {
        return result;
    }
}
