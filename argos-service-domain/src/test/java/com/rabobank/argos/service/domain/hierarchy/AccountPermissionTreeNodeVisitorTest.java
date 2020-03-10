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

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountPermissionTreeNodeVisitorTest {
    @Mock
    private AccountSecurityContext accountSecurityContext;

    private AccountPermissionTreeNodeVisitor accountPermissionTreeNodeVisitor;
    private static final String ROOT_ID = "rootId";
    private static final String CHILD_1_1_ID = "child1_1_Id";
    private static final String CHILD_1_2_ID = "child1_2_Id";
    private TreeNode root;
    private TreeNode child1_1;
    private TreeNode child1_2;
    private TreeNode child1_3;

    /**
     * Hierarchy tree node structure created for test
     * root---child1_1---child1_2---child1_3
     */
    @BeforeEach
    void setup() {

        root = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId(ROOT_ID)
                .hasChildren(true)
                .name("root")
                .build();
        child1_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .idPathToRoot(singletonList(ROOT_ID))
                .parentLabelId(ROOT_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_1_ID)
                .hasChildren(true)
                .name("child1_1")
                .build();

        child1_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_1_ID, ROOT_ID))
                .parentLabelId(CHILD_1_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_2_ID)
                .hasChildren(true)
                .name("child1_2")
                .build();

        child1_3 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_2_ID, CHILD_1_1_ID, ROOT_ID))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId(CHILD_1_2_ID)
                .hasChildren(false)
                .name("supplyCain")
                .build();
        createTreeNodeHierarchy();
        accountPermissionTreeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
    }

    @Test
    void visitEnter() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(Set.of(Permission.READ));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(root), is(true));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(child1_1), is(true));
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(true));
        assertThat(optionalTreeNode.get().getName(), is("root"));
        assertThat(optionalTreeNode.get().getChildren(), hasSize(1));
        assertThat(optionalTreeNode.get().getChildren().iterator().next().getName(), is("child1_1"));
    }

    @Test
    void visitExit() {
        assertThat(accountPermissionTreeNodeVisitor.visitExit(child1_3), is(true));
    }

    @Test
    void visitLeaf() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(Set.of(Permission.READ));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(child1_2), is(true));
        assertThat(accountPermissionTreeNodeVisitor.visitLeaf(child1_3), is(true));
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(true));
        assertThat(optionalTreeNode.get().getName(), is("child1_2"));
        assertThat(optionalTreeNode.get().getChildren(), hasSize(1));
        assertThat(optionalTreeNode.get().getChildren().iterator().next().getName(), is("supplyCain"));

    }

    private void createTreeNodeHierarchy() {
        root.addChild(child1_1);
        child1_1.addChild(child1_2);
        child1_2.addChild(child1_3);
    }
}