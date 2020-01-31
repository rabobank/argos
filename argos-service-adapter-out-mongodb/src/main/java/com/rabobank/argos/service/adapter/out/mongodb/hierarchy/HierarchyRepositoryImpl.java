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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
@Component
@Slf4j
public class HierarchyRepositoryImpl implements HierarchyRepository {
    private final MongoTemplate mongoTemplate;

    private static String PATH_TO_ROOT_QUERY;

    static {
        try {
            PATH_TO_ROOT_QUERY = IOUtils.toString(HierarchyRepositoryImpl.class
                    .getResourceAsStream("/db-query-scripts/pathtoroot-query.json"), UTF_8);
        } catch (IOException e) {
            log.error("error while reading query {}", e);
        }
    }

    @Override
    public List<String> getPathToRoot(String parentLabelId) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("labelIdToBeReplaced", parentLabelId);
        String queryCommand = StringSubstitutor.replace(PATH_TO_ROOT_QUERY, data);
        Document document = mongoTemplate.getDb().runCommand(Document.parse(queryCommand));
        List<String> pathToRoot = (List<String>) document.get("pathToRootIncluding");
        return pathToRoot;
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
