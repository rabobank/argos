package com.rabobank.argos.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;

class ArgosErrorTest {


    @Test
    void name() {
        Throwable throwable = new Throwable();
        ArgosError error = new ArgosError("message", throwable);
        assertThat(error.getMessage(), is("message"));
        assertThat(error.getCause(), sameInstance(throwable));
    }
}
