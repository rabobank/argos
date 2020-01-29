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
    @Builder.Default
    private Boolean result = false;

    @Override
    public boolean visitEnter(SupplyChainLabel supplyChainLabel) {
        log.debug("entered node: {} ", supplyChainLabel.getName());
        Optional<SupplyChainLabel> parentNode = supplyChainLabel.getChildren()
                    .stream()
                    .filter(lnode -> lnode.hasChildren() && lnode.getId().equals(parentRef))
                    .map(node -> (SupplyChainLabel) node)
                    .findFirst();

            parentNode.ifPresent((foundNode -> {
                foundNode.addChild(nodeToInsert);
                log.debug("child inserted into {}", foundNode.getName());
            }));
        if (parentNode.isPresent()) {
            result = true;
        }
        return !result;
    }

    @Override
    public boolean visitExit(SupplyChainLabel supplyChainLabel) {
        log.debug("exited node: {} ", supplyChainLabel.getName());
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
