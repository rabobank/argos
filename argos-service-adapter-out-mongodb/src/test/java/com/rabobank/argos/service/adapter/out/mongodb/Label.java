package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
@Setter
public class Label extends SupplyChainNode {

    private List<SupplyChainNode> children;

    @Override
    public Boolean hasChildren() {
        return true;
    }

    @Override
    public boolean accept(HierarchicalNodeVisitor hierarchicalNodeVisitor) {
        if (hierarchicalNodeVisitor.visitEnter(this)) {
            if (children != null) {
                children.stream()
                        .map(childNode -> childNode.accept(hierarchicalNodeVisitor))
                        .filter(result -> !result)
                        .findFirst();
            }
        }
        return hierarchicalNodeVisitor.visitExit(this);
    }

    @Override
    public Boolean isRoot() {
        return getParentNode() == null;
    }

    public void addChild(SupplyChainNode supplyChainNode) {
        supplyChainNode.setParentNode(this);
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(supplyChainNode);
    }
}
