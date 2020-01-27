package com.rabobank.argos.service.adapter.out.mongodb;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
public class SupplyChainRecord {

    enum Type {LABEL, SUPPLYCHAIN}

    private String id;
    private String parentId;
    private List<String> childIds;
    private int lft;
    private int rght;
    private String name;
    private LinkedList<String> namePathToRoot;
    private LinkedList<String> idPathToRoot;
    private int depth;
    private Type type;

}
