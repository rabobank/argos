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
import com.rabobank.argos.service.adapter.out.mongodb.hierarchy.HierarchyRepositoryImpl.HierarchyItem;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HierarchyRepositoryImplTest {

    private static final String COLLECTION = "hierarchy";
    private static final String ROOT_ID_1 = "rootId1";
    private static final String ROOT_ID_12 = "rootId12";
    private static final String SUB_1_ID = "sub1Id";
    private static final String SUB_SUB_2_1_ID = "sub-sub2-1Id";
    private static final String ROOT_1 = "root1";
    private static final String SUB_1 = "sub1";
    private static final String SUB_2 = "sub2";
    private static final String SUB_2_ID = "sub2Id";
    private static final String SUB_SUB_2_1 = "sub-sub2-1";
    private static final String ROOT_1_SUB_2 = "root1,sub2";
    private static final String ROOT_2 = "root2";
    @Mock
    private MongoTemplate template;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Captor
    private ArgumentCaptor<Aggregation> aggregationArgumentCaptor;

    @Mock
    private AggregationResults<HierarchyItem> aggregationResults;

    private HierarchyRepository hierarchyRepository;

    @BeforeEach
    void setup() {
        hierarchyRepository = new HierarchyRepositoryImpl(template);
    }

    @Test
    void getRootNodesWithHierarchyModeAllShouldReturnFullTrees() {
        createAggregationResultsFullTreeRootNodes();
        when(template.aggregate(any(Aggregation.class), ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)))
                .thenReturn(aggregationResults);
        List<TreeNode> rootNodes = hierarchyRepository.getRootNodes(HierarchyMode.ALL, 0);
        verify(template).aggregate(aggregationArgumentCaptor.capture(),
                ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)
        );
        assertThat(aggregationArgumentCaptor.getValue().toString(), is("{ \"aggregate\" : \"__collection__\", \"pipeline\" : [{ \"$match\" : { \"parentLabelId\" : null}}, { \"$graphLookup\" : { \"from\" : \"hierarchy\", \"startWith\" : \"$referenceId\", \"connectFromField\" : \"referenceId\", \"connectToField\" : \"parentLabelId\", \"as\" : \"descendants\", \"depthField\" : \"depth\"}}]}"));
        assertThat(rootNodes, hasSize(2));
        TreeNode root1 = rootNodes.get(0);
        TreeNode root2 = rootNodes.get(1);
        assertThat(root1.getReferenceId(), is(ROOT_ID_1));
        assertThat(root2.getReferenceId(), is(ROOT_ID_12));
        assertThat(root1.getChildren(), hasSize(2));
        assertThat(root2.getChildren(), hasSize(0));
        TreeNode sub1 = root1.getChildren().get(0);
        assertThat(sub1.getReferenceId(), is(SUB_1_ID));
        assertThat(sub1.getChildren(), hasSize(1));

    }

    @Test
    void getRootNodesWithHierarchyModeMaxDepthShouldReturnOnlyDirectChildren() {
        createAggregationResultsMaxDepthRootNodes();
        when(template.aggregate(aggregationArgumentCaptor.capture(), ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)))
                .thenReturn(aggregationResults);
        List<TreeNode> rootNodes = hierarchyRepository.getRootNodes(HierarchyMode.MAX_DEPTH, 1);
        verify(template).aggregate(any(Aggregation.class),
                ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)
        );

        assertThat(aggregationArgumentCaptor.getValue().toString(), containsString("maxDepth"));
        assertThat(rootNodes, hasSize(2));
        TreeNode root1 = rootNodes.get(0);
        TreeNode root2 = rootNodes.get(1);
        assertThat(root1.getReferenceId(), is(ROOT_ID_1));
        assertThat(root2.getReferenceId(), is(ROOT_ID_12));
        assertThat(root1.getChildren(), hasSize(2));
        assertThat(root2.getChildren(), hasSize(0));
        TreeNode sub1 = root1.getChildren().get(0);
        assertThat(sub1.getReferenceId(), is(SUB_1_ID));
        assertThat(sub1.getChildren(), hasSize(0));

    }


    @Test
    void getRootNodesWithHierarchyModeNoneShouldReturnOnlyDirectChildren() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        HierarchyItem root2 = createHierarchyItem(ROOT_2, ROOT_ID_12, null, emptyList());
        when(template.find(any(Query.class),
                ArgumentMatchers.eq(HierarchyItem.class),
                ArgumentMatchers.eq(COLLECTION))
        )
                .thenReturn(List.of(root1, root2));
        List<TreeNode> rootNodes = hierarchyRepository.getRootNodes(HierarchyMode.NONE, 0);
        verify(template).find(queryArgumentCaptor.capture(), ArgumentMatchers.eq(HierarchyItem.class),
                ArgumentMatchers.eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"parentLabelId\" : null}, Fields: {}, Sort: {}"));
        assertThat(rootNodes, hasSize(2));
        TreeNode treeNode1 = rootNodes.get(0);
        TreeNode treeNode2 = rootNodes.get(1);
        assertThat(treeNode1.getReferenceId(), is(ROOT_ID_1));
        assertThat(treeNode2.getReferenceId(), is(ROOT_ID_12));
        assertThat(treeNode1.getChildren(), hasSize(0));
    }

    @Test
    void getSubTreeWithHierarchyModeMaxDepthShouldReturnOnlyDirectChildren() {
        createAggregationResultsMaxDepthTreeSubTree();
        when(template.aggregate(any(Aggregation.class), ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)))
                .thenReturn(aggregationResults);
        Optional<TreeNode> subTree = hierarchyRepository.getSubTree(ROOT_ID_1, HierarchyMode.MAX_DEPTH, 1);

        verify(template).aggregate(aggregationArgumentCaptor.capture(),
                ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)
        );
        assertThat(aggregationArgumentCaptor.getValue().toString(), containsString("maxDepth"));
        assertThat(subTree.isPresent(), is(true));
        TreeNode root1 = subTree.get();
        assertThat(root1.getReferenceId(), is(ROOT_ID_1));
        assertThat(root1.getChildren(), hasSize(2));
        TreeNode sub1 = root1.getChildren().get(0);
        assertThat(sub1.getReferenceId(), is(SUB_1_ID));
        assertThat(sub1.getChildren(), hasSize(0));
    }


    @Test
    void getSubTreeWithHierarchyModeAllShouldReturnFullSubTree() {
        createAggregationResultsFullTreeSubTree();
        when(template.aggregate(any(Aggregation.class), ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)))
                .thenReturn(aggregationResults);
        Optional<TreeNode> subTree = hierarchyRepository.getSubTree(ROOT_ID_1, HierarchyMode.ALL, 0);
        verify(template).aggregate(aggregationArgumentCaptor.capture(),
                ArgumentMatchers.eq(COLLECTION),
                ArgumentMatchers.eq(HierarchyItem.class)
        );
        assertThat(aggregationArgumentCaptor.getValue().toString(), is("{ \"aggregate\" : \"__collection__\", \"pipeline\" : [{ \"$match\" : { \"referenceId\" : \"rootId1\"}}, { \"$graphLookup\" : { \"from\" : \"hierarchy\", \"startWith\" : \"$referenceId\", \"connectFromField\" : \"referenceId\", \"connectToField\" : \"parentLabelId\", \"as\" : \"descendants\", \"depthField\" : \"depth\"}}]}"));
        assertThat(subTree.isPresent(), is(true));
        TreeNode root1 = subTree.get();
        assertThat(root1.getReferenceId(), is(ROOT_ID_1));
        assertThat(root1.getName(), is(ROOT_1));
        assertThat(root1.getParentLabelId(), is(nullValue()));
        assertThat(root1.getPathToRoot(), hasSize(0));
        assertThat(root1.getChildren(), hasSize(2));
        TreeNode sub1 = root1.getChildren().get(0);
        assertThat(sub1.getReferenceId(), is(SUB_1_ID));
        assertThat(root1.getChildren(), hasSize(2));
        assertThat(sub1.getChildren(), hasSize(1));
    }

    @Test
    void getSubTreeWithHierarchyModeNoneShouldReturnSingleEntry() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        when(template.findOne(any(Query.class),
                ArgumentMatchers.eq(HierarchyItem.class),
                ArgumentMatchers.eq(COLLECTION)))
                .thenReturn(root1);

        Optional<TreeNode> subTree = hierarchyRepository.getSubTree(ROOT_ID_1, HierarchyMode.NONE, 0);
        verify(template).findOne(queryArgumentCaptor.capture(),
                ArgumentMatchers.eq(HierarchyItem.class),
                ArgumentMatchers.eq(COLLECTION)
        );
        assertThat(subTree.isPresent(), is(true));
        assertThat(subTree.get().getName(), is(ROOT_1));
        assertThat(subTree.get().getChildren(), hasSize(0));
    }

    void createAggregationResultsFullTreeSubTree() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        List<HierarchyItem> descendants = List.of(
                createHierarchyItem(SUB_1, SUB_1_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_2, SUB_2_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_SUB_2_1, SUB_SUB_2_1_ID, SUB_1_ID, List.of(ROOT_1_SUB_2))
        );

        root1.setDescendants(descendants);
        when(aggregationResults.getMappedResults()).thenReturn(List.of(root1));
    }

    void createAggregationResultsMaxDepthTreeSubTree() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        List<HierarchyItem> descendants = List.of(
                createHierarchyItem(SUB_1, SUB_1_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_2, SUB_2_ID, ROOT_ID_1, List.of(ROOT_1))

        );

        root1.setDescendants(descendants);
        when(aggregationResults.getMappedResults()).thenReturn(List.of(root1));
    }

    void createAggregationResultsFullTreeRootNodes() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        HierarchyItem root2 = createHierarchyItem(ROOT_2, ROOT_ID_12, null, emptyList());
        List<HierarchyItem> descendants = List.of(
                createHierarchyItem(SUB_1, SUB_1_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_2, SUB_2_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_SUB_2_1, SUB_SUB_2_1_ID, SUB_1_ID, List.of(ROOT_1_SUB_2))
        );

        root1.setDescendants(descendants);
        when(aggregationResults.getMappedResults()).thenReturn(List.of(root1, root2));
    }

    void createAggregationResultsMaxDepthRootNodes() {
        HierarchyItem root1 = createHierarchyItem(ROOT_1, ROOT_ID_1, null, emptyList());
        HierarchyItem root2 = createHierarchyItem(ROOT_2, ROOT_ID_12, null, emptyList());
        List<HierarchyItem> descendants = List.of(
                createHierarchyItem(SUB_1, SUB_1_ID, ROOT_ID_1, List.of(ROOT_1)),
                createHierarchyItem(SUB_2, SUB_2_ID, ROOT_ID_1, List.of(ROOT_1))
        );

        root1.setDescendants(descendants);
        when(aggregationResults.getMappedResults()).thenReturn(List.of(root1, root2));
    }

    private HierarchyItem createHierarchyItem(String name,
                                              String referenceId,
                                              String parentLabelId,
                                              List<String> pathToRoot) {
        HierarchyItem hierarchyItem = new HierarchyItem();
        hierarchyItem.setName(name);
        hierarchyItem.setParentLabelId(parentLabelId);
        hierarchyItem.setPathToRoot(pathToRoot);
        hierarchyItem.setReferenceId(referenceId);
        hierarchyItem.setType(HierarchyItem.Type.LABEL);
        return hierarchyItem;
    }
}