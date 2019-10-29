package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Argos4jSettings {

    public static final String DEFAULT_EXCLUDE_PATTERNS = "**.{git,link}**";

    private final String excludePatterns = DEFAULT_EXCLUDE_PATTERNS;
    private final boolean followSymlinkDirs;
    private final boolean normalizeLineEndings;

    private final String supplyChainId;
    private final String stepName;
    private final SigningKey signingKey;

    private final String baseUrl;
}
