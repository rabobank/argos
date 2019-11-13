package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.SignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestLinkServiceConfig {

    @Bean
    public SignatureValidator signatureValidator() {
        return new SignatureValidator();
    }
}
