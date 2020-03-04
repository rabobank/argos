package com.rabobank.argos.service.domain.hierarchy;

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;

public interface HierarchyService {

    TreeNode getSubTree(String referenceId, HierarchyMode hierarchyMode, Integer maxDepth);

}
