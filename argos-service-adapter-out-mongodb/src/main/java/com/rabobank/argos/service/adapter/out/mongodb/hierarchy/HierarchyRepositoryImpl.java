package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class HierarchyRepositoryImpl implements HierarchyRepository {
    @Override
    public List<String> getPathToRoot(String labelId) {

        //so
        //gr
        return null;
    }

    @Override
    public List<TreeNode> searchByName(String name, int depth) {
        return null;
    }

    @Override
    public TreeNode getSubTree(String id, int depth) {
        return null;
    }
}
