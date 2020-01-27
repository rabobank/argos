/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
