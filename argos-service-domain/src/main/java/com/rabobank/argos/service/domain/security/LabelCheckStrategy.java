package com.rabobank.argos.service.domain.security;

public interface LabelCheckStrategy {
    void checkLabelPermissions(LabelCheckData labelCheckData);
}
