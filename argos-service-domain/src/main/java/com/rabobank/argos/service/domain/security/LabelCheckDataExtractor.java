package com.rabobank.argos.service.domain.security;

import java.lang.reflect.Method;
import java.util.Optional;

public interface LabelCheckDataExtractor {
    Optional<LabelCheckData> extractLabelCheckData(Method method, Object[] argumentValues);
}
