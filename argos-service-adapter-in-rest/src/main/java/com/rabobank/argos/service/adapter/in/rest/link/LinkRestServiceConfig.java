package com.rabobank.argos.service.adapter.in.rest.link;

import com.rabobank.argos.domain.SignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkRestServiceConfig {

    @Bean
    public SignatureValidator signatureValidator() {
        return new SignatureValidator();
    }
}
