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
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class HierarchyServiceImplTest {

    public static final String ROOT_ID = "rootId";
    public static final String CHILD_1_1_ID = "child1_1_Id";
    public static final String CHILD_2_1_ID = "child2_1_Id";
    public static final String CHILD_1_2_ID = "child1_2_Id";
    public static final String CHILD_2_2_ID = "child2_2_id";
    @Mock
    private RoleRepository roleRepository;
    private Account account;

    @BeforeEach
    void setup() {
        List<LocalPermissions> localPermissions = new ArrayList<>();
        localPermissions.add(LocalPermissions
                .builder()
                .labelId(ROOT_ID)
                .permissions(List.of(Permission.READ))
                .build());

        account = PersonalAccount.builder()
                .name("test")

                .build();
        account.setLocalPermissions(localPermissions);
        when(roleRepository.findByIds(any())).thenReturn(emptyList());
    }

    @Test
    void getSubTree() {

        TreeNode root = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId(ROOT_ID)
                .hasChildren(true)
                .name("root")
                .build();

        TreeNode child1_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .idPathToRoot(singletonList(ROOT_ID))
                .parentLabelId(ROOT_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_1_ID)
                .hasChildren(true)
                .name("child1_1")
                .build();
        root.addChild(child1_1);

        TreeNode child2_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .parentLabelId(ROOT_ID)
                .idPathToRoot(singletonList(ROOT_ID))
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_2_1_ID)
                .hasChildren(true)
                .name("child2_1")
                .build();

        root.addChild(child2_1);

        TreeNode child1_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_1_ID, ROOT_ID))
                .parentLabelId(CHILD_1_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_2_ID)
                .hasChildren(true)
                .name("child1_2")
                .build();

        child1_1.addChild(child1_2);

        TreeNode child2_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "root"))
                .idPathToRoot(List.of(CHILD_2_1_ID, ROOT_ID))
                .type(TreeNode.Type.LABEL)
                .parentLabelId(CHILD_2_1_ID)
                .referenceId(CHILD_2_2_ID)
                .hasChildren(true)
                .name("child2_2")
                .build();

        child2_1.addChild(child2_2);

        TreeNode child1_3 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "child1_1", "root"))
                .idPathToRoot(List.of(CHILD_1_2_ID, CHILD_1_1_ID, ROOT_ID))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId(CHILD_1_2_ID)
                .hasChildren(false)
                .name("supplyCain")
                .build();

        child1_2.addChild(child1_3);

        TreeNodeVisitor<Optional<TreeNode>> permissionTreeNodeVisitor = new UserPermissionTreeNodeVisitor(account, roleRepository);
        root.accept(permissionTreeNodeVisitor);
        assertThat(permissionTreeNodeVisitor.result().isPresent(), is(true));
        TreeNode treeNodeWithPermissions = permissionTreeNodeVisitor.result().get();
        assertThat(treeNodeWithPermissions.getUserPermissions(), hasSize(1));
    }
}