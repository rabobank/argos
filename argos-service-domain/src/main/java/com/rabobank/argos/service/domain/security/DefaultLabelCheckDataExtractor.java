package com.rabobank.argos.service.domain.security;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class DefaultLabelCheckDataExtractor implements LabelCheckDataExtractor {
    @Override
    public LabelCheckData extractLabelCheckData(Method method) {
        return LabelCheckData.builder()
                .labelId("")
                .parentLabelId("")
                .build();
    }
}
