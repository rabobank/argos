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
package com.rabobank.argos.domain.hierarchy;

import com.rabobank.argos.domain.permission.Permission;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class TreeNode {
    public enum Type {
        LABEL(false), SUPPLY_CHAIN(true), NON_PERSONAL_ACCOUNT(true);
        private boolean isLeafNode;

        Type(boolean isLeafNode) {
            this.isLeafNode = isLeafNode;
        }

        boolean isLeafNode() {
            return this.isLeafNode;
        }
    }

    private String referenceId;
    private String name;
    private String parentLabelId;
    @With
    @Builder.Default
    private List<TreeNode> children = new ArrayList<>();
    private Type type;
    private List<String> pathToRoot;
    private List<String> idPathToRoot;
    private boolean hasChildren;

    @With
    private List<Permission> permissions;

    public boolean accept(TreeNodeVisitor treeNodeVisitor) {
        if (isLeafNode()) {
            return treeNodeVisitor.visitLeaf(this);
        } else if (treeNodeVisitor.visitEnter(this)) {
            children.forEach(child -> child.accept(treeNodeVisitor));

        }
        return treeNodeVisitor.visitExit(this);
    }

    public boolean isLeafNode() {
        return type.isLeafNode();
    }

    public void addChild(TreeNode treeNode) {
        children.add(treeNode);
    }
}
