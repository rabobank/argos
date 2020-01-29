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

@Slf4j
public class SupplyChainNodeUpDater implements HierarchicalNodeVisitor<SupplyChainNodeUpDater.UpdateResults> {
    private UpdateResults updateResults = UpdateResults.builder().build();
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
        log.debug("entered node: {} ", supplyChainNode.getName());
        currentNamePathToRoot.add(supplyChainNode.getName());
        currentDepth++;
        currentLeft = currentLeft + 1;
        int currentRight = currentLeft + (supplyChainNode.totalNumberOfDescendants() * 2) + 1;
        supplyChainNode.updateHierarchy(currentLeft, currentRight, currentDepth, currentIdPathToRoot, currentNamePathToRoot);
        SnapShot newState = SnapShot.copy(supplyChainNode);
        if (!backup.equals(newState)) {
            addToUpdateResults(supplyChainNode);
        }
        currentIdPathToRoot.add(supplyChainNode.getId());
        log.debug("updated hierarchy of {} to {} ", supplyChainNode.getName(), newState);
    }

    private void addToUpdateResults(SupplyChainNode supplyChainNode) {
        if (supplyChainNode.getId() == null) {
            supplyChainNode.setId(UUID.randomUUID().toString());
            updateResults.addCreated(supplyChainNode);
        } else {
            updateResults.addUpdated(supplyChainNode);
        }
    }

    @Override
    public boolean visitExit(SupplyChainLabel supplyChainLabel) {
        log.debug("exited node: {}  ", supplyChainLabel.getName());
        currentIdPathToRoot.remove(supplyChainLabel.getId());
        currentNamePathToRoot.remove(supplyChainLabel.getName());
        currentLeft = supplyChainLabel.getRght();
        currentDepth--;
        return true;
    }

    @Override
    public boolean visitLeaf(SupplyChain supplyChain) {
        onEnterNode(supplyChain);
        currentIdPathToRoot.remove(supplyChain.getId());
        currentNamePathToRoot.remove(supplyChain.getName());
        currentDepth--;
        return true;
    }

    @Override
    public UpdateResults result() {
        return updateResults;
    }

    @Data
    @Builder
    public static class UpdateResults {
        @Builder.Default
        List<SupplyChainTreeRecord> updatedRecords = new ArrayList<>();
        @Builder.Default
        List<SupplyChainTreeRecord> createdRecords = new ArrayList<>();
        @Builder.Default
        List<SupplyChainTreeRecord> removedRecords = new ArrayList<>();
        ;
        @Builder.Default
        List<SupplyChainNode> updated = new ArrayList<>();
        ;
        @Builder.Default
        List<SupplyChainNode> created = new ArrayList<>();
        ;
        @Builder.Default
        List<SupplyChainNode> removed = new ArrayList<>();
        ;

        void addUpdated(SupplyChainNode supplyChainNode) {
            log.debug("added {} to updated", supplyChainNode.getName());
            // SupplyChainRecord record = convertToSupplyChainRecord(supplyChainNode);
            updated.add(supplyChainNode);
        }

        void addCreated(SupplyChainNode supplyChainNode) {
            log.debug("added {} to created", supplyChainNode.getName());
            // SupplyChainRecord record = convertToSupplyChainRecord(supplyChainNode);
            created.add(supplyChainNode);
        }

        private SupplyChainTreeRecord convertToSupplyChainRecord(SupplyChainNode supplyChainNode) {
            SupplyChainTreeRecord record;
            if (supplyChainNode instanceof SupplyChain) {
                record = createSupplyChainRecordFromSupplyChain((SupplyChain) supplyChainNode);
            } else {
                record = createSupplyChainRecordFromSupplyChainLabel((SupplyChainLabel) supplyChainNode);
            }
            return record;
        }

        private SupplyChainTreeRecord createSupplyChainRecordFromSupplyChainLabel(SupplyChainLabel supplyChainNode) {

            return SupplyChainTreeRecord.builder()
                    .parentId(supplyChainNode.isRoot() ? null : supplyChainNode.getParentNode().getId())
                    .type(SupplyChainTreeRecord.Type.LABEL)
                    .id(supplyChainNode.getId())
                    .build();
        }

        private SupplyChainTreeRecord createSupplyChainRecordFromSupplyChain(SupplyChain supplyChainNode) {
            return SupplyChainTreeRecord.builder()
                    .parentId(supplyChainNode.getParentNode().getId())
                    .type(SupplyChainTreeRecord.Type.SUPPLYCHAIN)
                    .id(supplyChainNode.getId())
                    .build();
        }
    }


}
