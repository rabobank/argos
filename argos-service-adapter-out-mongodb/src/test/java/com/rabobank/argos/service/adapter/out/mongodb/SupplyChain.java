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
    public int totalNumberOfDescendants() {
        return 0;
    }

    @Override
    public Boolean isRoot() {
        return false;
    }
}
