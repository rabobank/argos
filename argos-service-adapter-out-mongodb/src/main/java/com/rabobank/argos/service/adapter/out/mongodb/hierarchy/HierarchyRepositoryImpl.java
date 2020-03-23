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

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

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
    private static final String PATH_TO_ROOT_FIELD = "pathToRoot";
    private static final String TYPE_FIELD = "type";
    private final MongoTemplate mongoTemplate;

    @Override
    public List<TreeNode> getRootNodes(HierarchyMode hierarchyMode, int maxDepth) {
        Criteria rootNodeCriteria = Criteria.where(PARENT_LABEL_ID).is(null);
        List<TreeNode> rootNodes = emptyList();
        final MatchOperation matchStage = Aggregation.match(rootNodeCriteria);
        switch (hierarchyMode) {
            case ALL:
                rootNodes = getRootNodesWithAllDescendants(matchStage);
                break;
            case NONE:
                rootNodes = getRootNodesWithNoDescendants(rootNodeCriteria);
                break;
            case MAX_DEPTH:
                rootNodes = getRootNodesWithMaxDepthDescendants(matchStage, maxDepth);
                break;
        }
        rootNodes.sort(Comparator.comparing(TreeNode::getName));
        return rootNodes;
    }

    private List<TreeNode> getRootNodesWithMaxDepthDescendants(MatchOperation matchStage, int maxDepth) {
        GraphLookupOperation graphLookupOperation = getGraphLookupOperationWithDepth(maxDepth);
        return queryWithAggregationForRootNodes(matchStage, graphLookupOperation);
    }

    private List<TreeNode> getRootNodesWithNoDescendants(Criteria rootNodeCriteria) {
        List<HierarchyItem> hierarchyItems = mongoTemplate.find(new Query(rootNodeCriteria), HierarchyItem.class, COLLECTION);
        return hierarchyItems
                .stream()
                .map(this::getTreeNode)
                .collect(Collectors.toList());
    }

    private List<TreeNode> getRootNodesWithAllDescendants(MatchOperation matchStage) {
        GraphLookupOperation graphLookupOperation = getGraphLookupOperationAllDescendants();
        return queryWithAggregationForRootNodes(matchStage, graphLookupOperation);
    }


    @Override
    public Optional<TreeNode> getSubTree(String referenceId, HierarchyMode hierarchyMode, int maxDepth) {
        final Criteria referenceCriteria = new Criteria(REFERENCE_ID).is(referenceId);
        final MatchOperation matchStage = Aggregation.match(referenceCriteria);
        switch (hierarchyMode) {
            case ALL:
                return getSubTreeWithAllDescendants(matchStage);
            case NONE:
                return getSubTreeWithNoDescendants(referenceCriteria);
            case MAX_DEPTH:
                return getSubTreeWithMaxDepthDescendants(matchStage, maxDepth);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TreeNode> findByNamePathToRootAndType(String name, List<String> pathToRoot, TreeNode.Type type) {
        Criteria pathTorootCriteria = Criteria.where(NAME_FIELD).is(name)
                .andOperator(Criteria.where(PATH_TO_ROOT_FIELD).is(pathToRoot), Criteria.where(TYPE_FIELD).is(type));
        HierarchyItem hierarchyItem = mongoTemplate.findOne(new Query(pathTorootCriteria), HierarchyItem.class, COLLECTION);
        if (hierarchyItem != null) {
            return convertToTreeNodeHierarchyForSubTree(List.of(hierarchyItem));
        } else {
            return Optional.empty();
        }
    }

    private Optional<TreeNode> getSubTreeWithNoDescendants(Criteria referenceCriteria) {
        List<HierarchyItem> hierarchyItems = Optional.ofNullable(mongoTemplate.findOne(new Query(referenceCriteria), HierarchyItem.class, COLLECTION))
                .map(List::of)
                .orElse(emptyList());
        return convertToTreeNodeHierarchyForSubTree(hierarchyItems);
    }

    private Optional<TreeNode> getSubTreeWithMaxDepthDescendants(MatchOperation matchStage, int maxDepth) {
        GraphLookupOperation graphLookupOperation = getGraphLookupOperationWithDepth(maxDepth);
        return queryWithAggregationForSubTree(matchStage, graphLookupOperation);
    }

    private GraphLookupOperation getGraphLookupOperationWithDepth(int maxDepth) {
        return GraphLookupOperation
                .builder()
                .from(COLLECTION)
                .startWith(START_REFERENCE_ID)
                .connectFrom(REFERENCE_ID)
                .connectTo(PARENT_LABEL_ID)
                /*  maxDepth 0 in GraphLookupOperation returns root plus immediate children.
                    This is counter intuitive so in the api it is minimum 1
                */
                .maxDepth((long) maxDepth - 1)
                .depthField(DEPTH)
                .as(DESCENDANTS);
    }

    private Optional<TreeNode> queryWithAggregationForSubTree(MatchOperation matchStage, GraphLookupOperation graphLookupOperation) {
        List<HierarchyItem> hierarchyResults = queryWithAggregation(matchStage, graphLookupOperation);
        return convertToTreeNodeHierarchyForSubTree(hierarchyResults);
    }

    private List<TreeNode> queryWithAggregationForRootNodes(MatchOperation matchStage, GraphLookupOperation graphLookupOperation) {
        List<HierarchyItem> hierarchyResults = queryWithAggregation(matchStage, graphLookupOperation);
        return hierarchyResults
                .stream()
                .map(this::getTreeNode)
                .collect(Collectors.toList());
    }

    private List<HierarchyItem> queryWithAggregation(MatchOperation matchStage, GraphLookupOperation graphLookupOperation) {
        Aggregation aggregation = Aggregation.newAggregation(matchStage, graphLookupOperation);
        return mongoTemplate.aggregate(aggregation, COLLECTION, HierarchyItem.class).getMappedResults();
    }

    private Optional<TreeNode> convertToTreeNodeHierarchyForSubTree(List<HierarchyItem> hierarchyResults) {
        if (!hierarchyResults.isEmpty()) {
            HierarchyItem rootItem = hierarchyResults.iterator().next();
            TreeNode root = getTreeNode(rootItem);
            return Optional.of(root);
        }
        return Optional.empty();
    }

    private TreeNode getTreeNode(HierarchyItem parentItem) {
        TreeNode parent = convertToTreeNode(parentItem);
        if (parentItem.getDescendants() != null) {
            List<TreeNode> descendants = parentItem
                    .getDescendants()
                    .stream()
                    .map(this::convertToTreeNode)
                    .collect(Collectors.toList());
            setChildren(parent, descendants);
        } else {
            parent.setChildren(emptyList());
        }
        return parent;
    }

    private Optional<TreeNode> getSubTreeWithAllDescendants(MatchOperation matchStage) {
        GraphLookupOperation graphLookupOperation = getGraphLookupOperationAllDescendants();
        return queryWithAggregationForSubTree(matchStage, graphLookupOperation);

    }

    private GraphLookupOperation getGraphLookupOperationAllDescendants() {
        return GraphLookupOperation
                .builder()
                .from(COLLECTION)
                .startWith(START_REFERENCE_ID)
                .connectFrom(REFERENCE_ID)
                .connectTo(PARENT_LABEL_ID)
                .depthField(DEPTH)
                .as(DESCENDANTS);
    }

    private TreeNode convertToTreeNode(HierarchyItem hierarchyItem) {
        return TreeNode
                .builder()
                .name(hierarchyItem.getName())
                .referenceId(hierarchyItem.getReferenceId())
                .type(TreeNode.Type.valueOf(hierarchyItem.getType().name()))
                .pathToRoot(hierarchyItem.getPathToRoot())
                .idPathToRoot(hierarchyItem.getIdPathToRoot())
                .idsOfDescendantLabels(hierarchyItem.getIdsOfDescendantLabels())
                .parentLabelId(hierarchyItem.getParentLabelId())
                .hasChildren(hierarchyItem.isHasChildren())
                .build();
    }

    private void setChildren(TreeNode parentItem, List<TreeNode> descendants) {
        Map<String, List<TreeNode>> descendantsByParenLabelId = descendants.stream()
                .filter(node -> node.getParentLabelId() != null)
                .collect(Collectors.groupingBy(TreeNode::getParentLabelId));
        List<TreeNode> children = getChildren(parentItem, descendantsByParenLabelId);
        parentItem.setChildren(children);
        descendants
                .forEach(descendant -> descendant.setChildren(getChildren(descendant, descendantsByParenLabelId)));
    }

    private List<TreeNode> getChildren(TreeNode parentItem, Map<String, List<TreeNode>> descendants) {
        List<TreeNode> children = descendants.getOrDefault(parentItem.getReferenceId(), emptyList());
        children.sort(Comparator.comparing(TreeNode::getName));
        return children;
    }

    @Getter
    @Setter
    static class HierarchyItem {
        enum Type {LABEL, SUPPLY_CHAIN, NON_PERSONAL_ACCOUNT}

        private String referenceId;
        private String name;
        private String parentLabelId;
        private Type type;
        private List<String> pathToRoot;
        private List<String> idPathToRoot;
        private List<String> idsOfDescendantLabels;
        private boolean hasChildren;
        private List<HierarchyItem> descendants;
        private int depth;
    }
}
