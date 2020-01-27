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

import com.rabobank.argos.service.adapter.out.mongodb.SupplyChainNode.SnapShot;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class SupplyChainNodeUpDater implements HierarchicalNodeVisitor<SupplyChainNodeUpDater.UpdateResults> {
    private UpdateResults updateResults = UpdateResults.builder()
            .created(new ArrayList<>())
            .removed(new ArrayList<>())
            .updated(new ArrayList<>())
            .build();
    private LinkedList<String> currentIdPathToRoot = new LinkedList<>();
    private LinkedList<String> currentNamePathToRoot = new LinkedList<>();
    private int currentDepth = 0;
    private int currentLeft = 0;
    @Override
    public boolean visitEnter(SupplyChainLabel supplyChainLabel) {
        onEnterNode(supplyChainLabel);
        return true;
    }

    private void onEnterNode(SupplyChainNode supplyChainNode) {
        SnapShot backup = SnapShot.copy(supplyChainNode);
        log.debug("entered node: {} ", backup);

        currentNamePathToRoot.add(supplyChainNode.getName());
        currentDepth++;
        currentLeft++;
        int right = currentLeft + ((supplyChainNode.totalNumberOfDescendants() + 1) * 2) + 1;
        supplyChainNode.updateHierarchy(currentLeft, right, currentDepth, currentIdPathToRoot, currentNamePathToRoot);
        SnapShot newState = SnapShot.copy(supplyChainNode);
        if (!backup.equals(newState)) {
            log.debug("updated hierarchy of {} to {} ", supplyChainNode.getName(), newState);
            addToUpdateResults(supplyChainNode);
        }
    }

    private void addToUpdateResults(SupplyChainNode supplyChainNode) {
        if (supplyChainNode.getId() == null) {
            updateResults.addCreated(supplyChainNode);
            supplyChainNode.setId(UUID.randomUUID().toString());
        } else {
            updateResults.addUpdated(supplyChainNode);
        }
        currentIdPathToRoot.add(supplyChainNode.getId());
    }

    @Override
    public boolean visitExit(SupplyChainLabel supplyChainLabel) {
        log.debug("exited node: {} {} {} {} ", supplyChainLabel.getName(), supplyChainLabel.getLft(), supplyChainLabel.getRght(), supplyChainLabel.getDepth());
        currentIdPathToRoot.remove(supplyChainLabel.getId());
        currentNamePathToRoot.remove(supplyChainLabel.getName());
        currentDepth--;
        return true;
    }

    @Override
    public boolean visitLeaf(SupplyChain supplyChain) {
        onEnterNode(supplyChain);
        return true;
    }

    @Override
    public UpdateResults result() {
        return updateResults;
    }

    @Data
    @Builder
    public static class UpdateResults {
        List<SupplyChainRecord> updated;
        List<SupplyChainRecord> created;
        List<SupplyChainRecord> removed;

        void addUpdated(SupplyChainNode supplyChainNode) {
            SupplyChainRecord record = convertToSupplyChainRecord(supplyChainNode);
            updated.add(record);
        }

        void addCreated(SupplyChainNode supplyChainNode) {
            SupplyChainRecord record = convertToSupplyChainRecord(supplyChainNode);
            created.add(record);
        }

        private SupplyChainRecord convertToSupplyChainRecord(SupplyChainNode supplyChainNode) {
            SupplyChainRecord record;
            if (supplyChainNode instanceof SupplyChain) {
                record = createSupplyChainRecordFromSupplyChain((SupplyChain) supplyChainNode);
            } else {
                record = createSupplyChainRecordFromSupplyChainLabel((SupplyChainLabel) supplyChainNode);
            }
            return record;
        }

        private SupplyChainRecord createSupplyChainRecordFromSupplyChainLabel(SupplyChainLabel supplyChainNode) {

            return SupplyChainRecord.builder()
                    .childIds(supplyChainNode.getChildren().stream()
                            .map(child -> child.getId()).collect(Collectors.toCollection(LinkedList::new))
                    )
                    .namePathToRoot(supplyChainNode.getNamePathToRoot())
                    .idPathToRoot(supplyChainNode.getIdPathToRoot())
                    .depth(supplyChainNode.getDepth())
                    .lft(supplyChainNode.getLft())
                    .rght(supplyChainNode.getRght())
                    .parentId(supplyChainNode.isRoot() ? null : supplyChainNode.getParentNode().getId())
                    .type(SupplyChainRecord.Type.LABEL)
                    .id(supplyChainNode.getId())
                    .name(supplyChainNode.getName())
                    .build();
        }

        private SupplyChainRecord createSupplyChainRecordFromSupplyChain(SupplyChain supplyChainNode) {
            return SupplyChainRecord.builder()
                    .namePathToRoot(supplyChainNode.getNamePathToRoot())
                    .idPathToRoot(supplyChainNode.getIdPathToRoot())
                    .depth(supplyChainNode.getDepth())
                    .lft(supplyChainNode.getLft())
                    .rght(supplyChainNode.getRght())
                    .parentId(supplyChainNode.getParentNode().getId())
                    .type(SupplyChainRecord.Type.SUPPLYCHAIN)
                    .id(supplyChainNode.getId())
                    .name(supplyChainNode.getName())
                    .build();
        }
    }


}
