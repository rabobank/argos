package com.rabobank.argos.service.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class ServiceDomainConfigTest {

    @Test
    void signatureValidator() {
        ServiceDomainConfig serviceDomainConfig = new ServiceDomainConfig();
        assertThat(serviceDomainConfig.signatureValidator(), is(notNullValue()));
    }
}