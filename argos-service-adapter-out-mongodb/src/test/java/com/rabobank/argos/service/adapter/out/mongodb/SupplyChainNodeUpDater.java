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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SupplyChainNodeUpDater implements HierarchicalNodeVisitor<SupplyChainNodeUpDater.UpdateResults> {
    private UpdateResults updateResults;

    @Override
    public boolean visitEnter(Label label) {
        //storeCurrent(label);
        //updateLevel(label);
        // updateLeftRight(label)
        //
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
        List<SupplyChainNode> removed;
    }
}
