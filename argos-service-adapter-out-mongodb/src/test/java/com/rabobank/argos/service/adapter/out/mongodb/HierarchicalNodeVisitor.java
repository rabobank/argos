package com.rabobank.argos.service.adapter.out.mongodb;

public interface HierarchicalNodeVisitor<R> {
    boolean visitEnter(Label label);

    boolean visitExit(Label label);

    boolean visitLeaf(SupplyChain supplyChain);

    R result();
}
