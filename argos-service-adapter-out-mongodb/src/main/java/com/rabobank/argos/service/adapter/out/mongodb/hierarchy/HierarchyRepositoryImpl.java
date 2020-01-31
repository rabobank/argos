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
package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class HierarchyRepositoryImpl implements HierarchyRepository {
    @Override
    public List<String> getPathToRoot(String parentLabelId) {

        //so
        //gr
        return Collections.emptyList();
    }

    @Override
    public List<TreeNode> searchByName(String name) {

        return Collections.emptyList();
    }

    @Override
    public TreeNode getSubTree(String id, int depth) {
        return null;
    }
}
