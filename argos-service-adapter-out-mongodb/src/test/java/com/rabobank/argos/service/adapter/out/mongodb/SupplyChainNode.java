package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

@SuperBuilder
@Getter
@Setter
public abstract class SupplyChainNode {

    private String id;
    private Integer lft;
    private Integer rght;
    private Integer depth;
    private String name;
    private List<String> idPathToRoot = new LinkedList<>();
    private List<String> namePathToRoot = new LinkedList<>();
    private SupplyChainNode parentNode;

    public abstract Boolean hasChildren();

    public abstract boolean accept(HierarchicalNodeVisitor hierarchicalNodeVisitor);

    public abstract Boolean isRoot();
}

