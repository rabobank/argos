package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SupplyChain extends SupplyChainNode {

    @Override
    public Boolean hasChildren() {
        return false;
    }

    @Override
    public boolean accept(HierarchicalNodeVisitor hierarchicalNodeVisitor) {
        return hierarchicalNodeVisitor.visitLeaf(this);
    }

    @Override
    public Boolean isRoot() {
        return false;
    }
}
