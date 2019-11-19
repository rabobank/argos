package com.rabobank.argos.domain.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

class PathHelperTest {

    @Test
    void normalizePathWithNullShouldReturnNull() {
        assertThat(PathHelper.normalizePath(null), is(nullValue()));
    }

    @Test
    void normalizePathWithNotNullShouldReturnNormalized() {
        assertThat(PathHelper.normalizePath("\\\\path"), is("/path"));
    }
}