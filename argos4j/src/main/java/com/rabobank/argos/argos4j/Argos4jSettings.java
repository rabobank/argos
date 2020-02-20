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
package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
public class Argos4jSettings implements Serializable {

    public static final String DEFAULT_EXCLUDE_PATTERNS = "**.{git,link}**";

    @Builder.Default
    private final String excludePatterns = DEFAULT_EXCLUDE_PATTERNS;

    @Builder.Default
    private final boolean followSymlinkDirs = true;

    @Builder.Default
    private final boolean normalizeLineEndings = false;

    private final String supplyChainName;
    private final List<String> pathToLabelRoot;
    private final String runId;
    private final String layoutSegmentName;
    private final String stepName;
    private final String signingKeyId;
    private final String argosServerBaseUrl;

}
