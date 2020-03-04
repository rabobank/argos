package com.rabobank.argos.service.domain.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.domain.permission.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserPermissionTreeNodeVisitor implements TreeNodeVisitor<TreeNode> {

    private TreeNode treeNodeWithUserPermissions;

    private HashMap<String, TreeNode> parentRegistry = new HashMap<>();

    @Override
    public boolean visitEnter(TreeNode treeNode) {

        TreeNode copyOfTreeNode = treeNode
                .withChildren(new ArrayList<>())
                .withUserPermissions(determineAggregatedPermissions(treeNode));

        if (treeNodeWithUserPermissions == null) {
            treeNodeWithUserPermissions = copyOfTreeNode;
        } else {
            TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
            parent.addChild(copyOfTreeNode);
        }
        parentRegistry.put(copyOfTreeNode.getReferenceId(), copyOfTreeNode);
        return true;
    }

    private List<Permission> determineAggregatedPermissions(TreeNode treeNode) {
        return List.of(Permission.READ);
    }

    @Override
    public boolean visitExit(TreeNode treeNode) {
        return true;
    }

    @Override
    public boolean visitLeaf(TreeNode treeNode) {
        TreeNode copyOfTreeNode = treeNode.withUserPermissions(determineAggregatedPermissions(treeNode));
        TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
        parent.addChild(copyOfTreeNode);
        return true;
    }

    @Override
    public TreeNode result() {
        return treeNodeWithUserPermissions;
    }
}
