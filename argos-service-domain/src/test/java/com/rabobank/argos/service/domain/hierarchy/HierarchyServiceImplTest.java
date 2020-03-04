package com.rabobank.argos.service.domain.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class HierarchyServiceImplTest {

    @Test
    void getSubTree() {

        TreeNode root = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId("rootId")
                .hasChildren(true)
                .name("root")
                .build();

        TreeNode child1_1 = TreeNode.builder()
                .pathToRoot(singletonList("rootId"))
                .idPathToRoot(singletonList("root"))
                .parentLabelId("rootId")
                .type(TreeNode.Type.LABEL)
                .referenceId("child1_1_Id")
                .hasChildren(true)
                .name("child1_1")
                .build();
        root.addChild(child1_1);

        TreeNode child2_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .parentLabelId("rootId")
                .idPathToRoot(singletonList("rootId"))
                .type(TreeNode.Type.LABEL)
                .referenceId("child2_1_Id")
                .hasChildren(true)
                .name("child2_1")
                .build();

        root.addChild(child2_1);

        TreeNode child1_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_1", "root"))
                .idPathToRoot(List.of("child1_1_Id", "rootId"))
                .parentLabelId("child1_1_Id")
                .type(TreeNode.Type.LABEL)
                .referenceId("child1_2_Id")
                .hasChildren(true)
                .name("child1_2")
                .build();

        child1_1.addChild(child1_2);

        TreeNode child2_2 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "root"))
                .idPathToRoot(List.of("child1_2_Id", "rootId"))
                .type(TreeNode.Type.LABEL)
                .parentLabelId("child2_1_Id")
                .referenceId("child2_2_id")
                .hasChildren(true)
                .name("child2_2")
                .build();

        child2_1.addChild(child2_2);

        TreeNode child1_3 = TreeNode.builder()
                .pathToRoot(List.of("child1_2", "child1_1", "root"))
                .idPathToRoot(List.of("child1_2", "child1_1_Id", "rootId"))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId("child1_2_Id")
                .hasChildren(false)
                .name("supplyCain")
                .build();

        child1_2.addChild(child1_3);
        TreeNodeVisitor<TreeNode> permissionTreeNodeVisitor = new UserPermissionTreeNodeVisitor();
        root.accept(permissionTreeNodeVisitor);
        TreeNode treeNodeWithPermissions = permissionTreeNodeVisitor.result();
    }
}