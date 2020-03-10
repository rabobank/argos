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

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyServiceImpl implements HierarchyService {

    private final HierarchyRepository hierarchyRepository;
    private final AccountSecurityContext accountSecurityContext;

    @Override
    public Optional<TreeNode> getSubTree(String referenceId, HierarchyMode hierarchyMode, Integer maxDepth) {
        TreeNodeVisitor<Optional<TreeNode>> treeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
        hierarchyRepository
                .getSubTree(referenceId, hierarchyMode, maxDepth)
                .ifPresent(treeNode -> treeNode.accept(treeNodeVisitor)
                );

        return treeNodeVisitor.result();
    }

    @Override
    public List<TreeNode> getRootNodes(HierarchyMode hierarchyMode, int maxDepth) {
        return hierarchyRepository
                .getRootNodes(hierarchyMode, maxDepth)
                .stream()
                .map(treeNode -> {
                    TreeNodeVisitor<Optional<TreeNode>> treeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
                            treeNode.accept(treeNodeVisitor);
                            return treeNodeVisitor.result();
                        }
                ).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
