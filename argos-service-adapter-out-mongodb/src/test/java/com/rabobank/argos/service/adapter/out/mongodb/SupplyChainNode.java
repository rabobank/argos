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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode
public abstract class SupplyChainNode {

    private String id;
    private Integer lft;
    private Integer rght;
    private Integer depth;
    private String name;
    @Builder.Default
    private LinkedList<String> idPathToRoot = new LinkedList<>();
    @Builder.Default
    private LinkedList<String> namePathToRoot = new LinkedList<>();

    private SupplyChainNode parentNode;

    public abstract Boolean hasChildren();

    public abstract boolean accept(HierarchicalNodeVisitor hierarchicalNodeVisitor);

    public abstract int totalNumberOfDescendants();
    public abstract Boolean isRoot();

    public void updateHierarchy(int left, int right, int depth, LinkedList<String> idPathToRoot, LinkedList<String> namePathToRoot) {
        this.lft = left;
        this.rght = right;
        this.depth = depth;
        this.idPathToRoot = new LinkedList<>(idPathToRoot);
        this.namePathToRoot = new LinkedList<>(namePathToRoot);
    }

    @Builder
    @EqualsAndHashCode
    @ToString
    static class SnapShot {
        private Integer lft;
        private Integer rght;
        private Integer depth;
        private List<String> idPathToRoot;
        private List<String> namePathToRoot;

        static SnapShot copy(SupplyChainNode supplyChainNode) {

            return SnapShot
                    .builder()
                    .depth(supplyChainNode.depth)
                    .lft(supplyChainNode.getLft())
                    .rght(supplyChainNode.getRght())
                    .idPathToRoot(unmodifiableList(
                            supplyChainNode.getIdPathToRoot()
                    ))
                    .namePathToRoot(
                            unmodifiableList(supplyChainNode.getNamePathToRoot()
                            ))
                    .build();
        }
    }
}

