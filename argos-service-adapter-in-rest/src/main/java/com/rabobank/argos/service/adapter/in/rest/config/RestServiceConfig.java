package com.rabobank.argos.service.adapter.in.rest.config;

import com.rabobank.argos.domain.signing.SignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestServiceConfig {

    @Bean
    public SignatureValidator signatureValidator() {
        return new SignatureValidator();
    }
}
