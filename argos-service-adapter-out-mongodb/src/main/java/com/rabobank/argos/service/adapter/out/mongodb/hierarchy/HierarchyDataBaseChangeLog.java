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

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.apache.commons.io.IOUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@ChangeLog
public class HierarchyDataBaseChangeLog {
    @ChangeSet(order = "001", id = "HierarchyChangelog-1", author = "michel")
    public void createHierarchyView(MongoTemplate template) throws IOException {
        String createViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-scripts/create-hierarchy-view.json"), UTF_8);

        template.getDb().getCollection("hierarchy").drop();
        template.executeCommand(createViewCommand);
    }
}
