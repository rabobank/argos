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
package com.rabobank.argos.service.domain.hierarchy;


import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import com.rabobank.argos.service.domain.security.AccountSecurityContextImpl;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HierarchyServiceImplTest {

    private static final String ROOT_1_ID = "root_1_Id";
    private static final String CHILD_1_1_ID = "child1_1_Id";
    private static final String CHILD_2_1_ID = "child2_1_Id";
    private static final String CHILD_1_2_ID = "child1_2_Id";
    private static final String CHILD_2_2_ID = "child2_2_id";


    private static final String ROOT_2_ID = "root_2_Id";
    private static final String CHILD_2_1_1_ID = "child2_1_1_Id";
    private static final String CHILD_2_2_1_ID = "child2_2_1_Id";
    private static final String CHILD_2_1_2_ID = "child2_1_2_Id";
    private static final String CHILD_2_2_2_ID = "child2_2_2_id";


    private AccountSecurityContext accountSecurityContext;

    @Mock
    private HierarchyRepository hierarchyRepository;

    private HierarchyService hierarchyService;

    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;

    @Mock
    private Account account;

    @Mock
    private Authentication authentication;

    private TreeNode root_1;
    private TreeNode child1_1;
    private TreeNode child2_1;
    private TreeNode child1_2;
    private TreeNode child2_2;
    private TreeNode child1_3;

    private TreeNode root_2;
    private TreeNode child_2_1_1;
    private TreeNode child_2_2_1;
    private TreeNode child_2_1_2;
    private TreeNode child_2_2_2;
    private TreeNode child_2_1_3;

    /*
     *   Hierarchy tree node structure created for test
     *
     *          --- child1_1 --- child1_2 ---  child1_3
     * root_1
     *          --- child2_1 --- child2_2
     *
     *
     *          --- child_2_1_1 --- child_2_1_2 ---  child_2_1_3
     * root_2
     *          --- child_2_2_1 --- child_2_2_2
     */
    @BeforeEach
    void setup() {
        createRootNode1();
        createRootNode2();
        accountSecurityContext = new AccountSecurityContextImpl();
        hierarchyService = new HierarchyServiceImpl(hierarchyRepository, accountSecurityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
    private void createRootNode1() {
        root_1 = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId(ROOT_1_ID)
                .hasChildren(true)
                .name("root")
                .build();
        child1_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .idPathToRoot(singletonList(ROOT_1_ID))
                .parentLabelId(ROOT_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_1_ID)
                .hasChildren(true)
                .name("child1_1")
                .build();
        child2_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .parentLabelId(ROOT_1_ID)
                .idPathToRoot(singletonList(ROOT_1_ID))
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_2_1_ID)
                .hasChildren(true)
                .name("child2_1")
                .build();
        child1_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_1_ID, ROOT_1_ID))
                .parentLabelId(CHILD_1_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_2_ID)
                .hasChildren(true)
                .name("child1_2")
                .build();
        child2_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "root"))
                .idPathToRoot(List.of(CHILD_2_1_ID, ROOT_1_ID))
                .type(TreeNode.Type.LABEL)
                .parentLabelId(CHILD_2_1_ID)
                .referenceId(CHILD_2_2_ID)
                .hasChildren(true)
                .name("child2_2")
                .build();
        child1_3 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_2_ID, CHILD_1_1_ID, ROOT_1_ID))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId(CHILD_1_2_ID)
                .hasChildren(false)
                .name("supplyCain")
                .build();
    }

    private void createRootNode2() {
        root_2 = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId(ROOT_2_ID)
                .hasChildren(true)
                .name("root")
                .build();
        child_2_1_1 = TreeNode.builder()
                .pathToRoot(singletonList("root2"))
                .idPathToRoot(singletonList(ROOT_2_ID))
                .parentLabelId(ROOT_2_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_2_1_1_ID)
                .hasChildren(true)
                .name("child1_1")
                .build();
        child_2_2_1 = TreeNode.builder()
                .pathToRoot(singletonList("root2"))
                .parentLabelId(ROOT_2_ID)
                .idPathToRoot(singletonList(ROOT_2_ID))
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_2_1_ID)
                .hasChildren(true)
                .name("child2_1")
                .build();
        child_2_1_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_1", "root2"))
                .idPathToRoot(List.of(CHILD_2_1_1_ID, ROOT_2_ID))
                .parentLabelId(CHILD_1_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_2_ID)
                .hasChildren(true)
                .name("child1_2")
                .build();
        child_2_2_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "root"))
                .idPathToRoot(List.of(CHILD_2_2_1_ID, ROOT_2_ID))
                .type(TreeNode.Type.LABEL)
                .parentLabelId(CHILD_2_2_1_ID)
                .referenceId(CHILD_2_2_2_ID)
                .hasChildren(true)
                .name("child2_2")
                .build();
        child_2_1_3 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "child1_1", "root2"))
                .idPathToRoot(List.of(CHILD_2_1_2_ID, CHILD_2_1_1_ID, ROOT_2_ID))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId(CHILD_2_1_2_ID)
                .hasChildren(false)
                .name("supplyCain")
                .build();
    }

    @Test
    void getSubTreeShouldReturnTreeNodeWithPermissions() {
        createTreeNodeHierarchy();
        List<LocalPermissions> localPermissions = Collections.singletonList(LocalPermissions.builder()
                .labelId(ROOT_1_ID)
                .permissions(List.of(Permission.READ))
                .build());
        when(account.getLocalPermissions()).thenReturn(localPermissions);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(Set.of(Permission.TREE_EDIT));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(hierarchyRepository.getSubTree(ROOT_1_ID, HierarchyMode.ALL, 0)).thenReturn(Optional.of(root_1));
        Optional<TreeNode> optionalTreeNode = hierarchyService.getSubTree(ROOT_1_ID, HierarchyMode.ALL, 0);
        assertThat(optionalTreeNode.isPresent(), is(true));
        TreeNode treeNodeWithPermissions = optionalTreeNode.get();
        assertThat(treeNodeWithPermissions.getPermissions(), hasSize(2));
        assertThat(treeNodeWithPermissions.getChildren(), hasSize(2));
        assertThat(treeNodeWithPermissions.getChildren().iterator().next().getPermissions(), hasSize(2));


    }

    @Test
    void getSubTreeWithNoPermissionsShouldReturnEmpty() {
        createTreeNodeHierarchy();
        when(account.getLocalPermissions()).thenReturn(emptyList());
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(emptySet());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(hierarchyRepository.getSubTree(ROOT_1_ID, HierarchyMode.ALL, 0)).thenReturn(Optional.of(root_1));
        Optional<TreeNode> optionalTreeNode = hierarchyService.getSubTree(ROOT_1_ID, HierarchyMode.ALL, 0);
        assertThat(optionalTreeNode.isEmpty(), is(true));
    }


    @Test
    void getRootNodesWithPartialPermissionsShouldFilterResult() {
        createTreeNodeHierarchy();
        List<LocalPermissions> localPermissions = Collections.singletonList(LocalPermissions.builder()
                .labelId(ROOT_1_ID)
                .permissions(List.of(Permission.READ))
                .build());
        when(account.getLocalPermissions()).thenReturn(localPermissions);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(emptySet());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(hierarchyRepository.getRootNodes(HierarchyMode.ALL, 0)).thenReturn(List.of(root_1, root_2));
        List<TreeNode> rootNodes = hierarchyService.getRootNodes(HierarchyMode.ALL, 0);
        assertThat(rootNodes.isEmpty(), is(false));
        assertThat(rootNodes, hasSize(1));
    }


    @Test
    void getRootNodesWithDifferentPermissionsUpTreeShouldResultInCorrectPermissions() {
        createTreeNodeHierarchy();
        List<LocalPermissions> localPermissions = List.of(
                LocalPermissions
                        .builder()
                        .labelId(ROOT_1_ID)
                        .permissions(List.of(Permission.READ))
                        .build(),
                LocalPermissions
                        .builder()
                        .labelId(CHILD_2_1_ID)
                        .permissions(List.of(Permission.TREE_EDIT))
                        .build()

        );
        when(account.getLocalPermissions()).thenReturn(localPermissions);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(emptySet());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(accountSecurityContext.getGlobalPermission()).thenReturn(emptySet());
        when(hierarchyRepository.getRootNodes(HierarchyMode.ALL, 0)).thenReturn(List.of(root_1, root_2));
        List<TreeNode> rootNodes = hierarchyService.getRootNodes(HierarchyMode.ALL, 0);
        assertThat(rootNodes.isEmpty(), is(false));
        assertThat(rootNodes, hasSize(1));
        TreeNode childWithoutAddedPermissions = rootNodes.iterator().next().getChildren().get(0);
        TreeNode childWithAddedPermissions = rootNodes.iterator().next().getChildren().get(1);
        assertThat(childWithoutAddedPermissions.getPermissions(), is(List.of(Permission.READ)));
        assertThat(childWithAddedPermissions.getPermissions(), is(List.of(Permission.READ, Permission.TREE_EDIT)));
    }

    private void createTreeNodeHierarchy() {
        root_1.addChild(child1_1);
        root_1.addChild(child2_1);
        child1_1.addChild(child1_2);
        child1_2.addChild(child1_3);
        child2_1.addChild(child2_2);

        root_2.addChild(child_2_1_1);
        root_2.addChild(child_2_2_1);
        child_2_1_1.addChild(child_2_1_2);
        child_2_1_2.addChild(child_2_1_3);
        child_2_2_1.addChild(child_2_2_2);
    }
}