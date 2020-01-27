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
