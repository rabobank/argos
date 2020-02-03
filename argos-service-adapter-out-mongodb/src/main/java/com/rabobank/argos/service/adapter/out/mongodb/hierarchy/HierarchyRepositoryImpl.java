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
package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class HierarchyRepositoryImpl implements HierarchyRepository {
    private static final String COLLECTION = "hierarchy";
    private static final String REFERENCE_ID = "referenceId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String DEPTH = "depth";
    private static final String DESCENDANTS = "descendants";
    private static final String START_REFERENCE_ID = "$referenceId";
    private static final String NAME_FIELD = "name";
    private final MongoTemplate mongoTemplate;

    @Override
    public List<TreeNode> searchByName(String name) {
        Criteria criteria = Criteria.where(NAME_FIELD).regex(".*" + name + ".*", "i");

        List<HierarchyItem> results = mongoTemplate.find(new Query(criteria),
                HierarchyItem.class,
                COLLECTION);

        return results.stream().map(this::convertToTreeNode)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TreeNode> getSubTree(String id, int depth) {
        final Criteria referenceId = new Criteria(REFERENCE_ID).is(id);
        final MatchOperation matchStage = Aggregation.match(referenceId);
        GraphLookupOperation graphLookupOperation;
        if (depth == -1) {
            graphLookupOperation = GraphLookupOperation
                    .builder()
                    .from(COLLECTION)
                    .startWith(START_REFERENCE_ID)
                    .connectFrom(REFERENCE_ID)
                    .connectTo(PARENT_LABEL_ID)
                    .depthField(DEPTH)
                    .as(DESCENDANTS);
        } else {
            graphLookupOperation = GraphLookupOperation
                    .builder()
                    .from(COLLECTION)
                    .startWith(START_REFERENCE_ID)
                    .connectFrom(REFERENCE_ID)
                    .connectTo(PARENT_LABEL_ID)
                    .maxDepth(depth)
                    .depthField(DEPTH)
                    .as(DESCENDANTS);
        }
        Aggregation aggregation = Aggregation.newAggregation(matchStage, graphLookupOperation);
        List<HierarchyItem> hierarchyResults = mongoTemplate.aggregate(aggregation, COLLECTION, HierarchyItem.class).getMappedResults();
        if (!hierarchyResults.isEmpty()) {
            HierarchyItem rootItem = hierarchyResults.iterator().next();
            TreeNode root = convertToTreeNode(rootItem);
            List<TreeNode> descendants = rootItem
                    .getDescendants()
                    .stream()
                    .map(this::convertToTreeNode)
                    .collect(Collectors.toList());
            setChildren(root, descendants);
            return Optional.of(root);
        }
        return Optional.empty();
    }

    private TreeNode convertToTreeNode(HierarchyItem hierarchyItem) {
        return TreeNode
                .builder()
                .name(hierarchyItem.getName())
                .referenceId(hierarchyItem.getReferenceId())
                .type(TreeNode.Type.valueOf(hierarchyItem.getType().name()))
                .pathToRoot(hierarchyItem.getPathToRoot())
                .parentLabelId(hierarchyItem.getParentLabelId())
                .build();
    }

    private void setChildren(TreeNode parentItem, List<TreeNode> descendants) {
        Map<String, List<TreeNode>> descendantsByParenLabelId = descendants.stream()
                .collect(Collectors.groupingBy(TreeNode::getParentLabelId));
        List<TreeNode> children = getChildren(parentItem, descendantsByParenLabelId);
        parentItem.setChildren(children);
        descendants
                .forEach(descendant -> descendant.setChildren(getChildren(descendant, descendantsByParenLabelId)));
    }

    private List<TreeNode> getChildren(TreeNode parentItem, Map<String, List<TreeNode>> descendants) {
        return descendants.getOrDefault(parentItem.getReferenceId(), Collections.emptyList());
    }

    @Getter
    @Setter
    public static class HierarchyItem {

        public enum Type {LABEL, SUPPLY_CHAIN}

        private String referenceId;
        private String name;
        private String parentLabelId;
        private Type type;
        private List<String> pathToRoot;
        private List<HierarchyItem> descendants;
        private int depth;
    }
}
