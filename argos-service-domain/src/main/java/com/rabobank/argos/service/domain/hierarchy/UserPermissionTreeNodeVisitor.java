package com.rabobank.argos.service.domain.hierarchy;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.permission.RoleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class UserPermissionTreeNodeVisitor implements TreeNodeVisitor<Optional<TreeNode>> {

    private TreeNode treeNodeWithUserPermissions;
    private HashMap<String, TreeNode> parentRegistry = new HashMap<>();
    private Set<Permission> globalPermissions = Collections.emptySet();
    private final Account account;

    public UserPermissionTreeNodeVisitor(Account account, RoleRepository roleRepository) {
        this.account = account;
        if (account instanceof PersonalAccount) {
            globalPermissions = roleRepository
                    .findByIds(((PersonalAccount) account).getRoleIds())
                    .stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean visitEnter(TreeNode treeNode) {
        TreeNode copyOfTreeNode = treeNode
                .withChildren(new ArrayList<>())
                .withUserPermissions(determineAggregatedPermissions(treeNode));

        if (copyOfTreeNode.getUserPermissions().isEmpty()) {
            return false;
        }
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
        List<String> labelIdsUpTree = new ArrayList<>(treeNode.getIdPathToRoot());
        if (!treeNode.isLeafNode()) {
            labelIdsUpTree.add(treeNode.getReferenceId());
        }
        Set<Permission> localPermissions = account.allLocalPermissions(labelIdsUpTree);
        localPermissions.addAll(globalPermissions);
        return new ArrayList<>(localPermissions);
    }

    @Override
    public boolean visitExit(TreeNode treeNode) {
        return true;
    }

    @Override
    public boolean visitLeaf(TreeNode treeNode) {
        TreeNode copyOfTreeNode = treeNode.withUserPermissions(determineAggregatedPermissions(treeNode));

        if (copyOfTreeNode.getUserPermissions().isEmpty()) {
            return false;
        }
        TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
        parent.addChild(copyOfTreeNode);
        return true;
    }

    @Override
    public Optional<TreeNode> result() {
        return Optional.ofNullable(treeNodeWithUserPermissions);
    }
}
