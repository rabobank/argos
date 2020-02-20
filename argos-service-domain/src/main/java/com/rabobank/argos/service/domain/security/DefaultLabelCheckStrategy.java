package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultLabelCheckStrategy implements LabelCheckStrategy {

    private final HierarchyRepository hierarchyRepository;

    @Override
    public void checkLabelPermissions(LabelCheckData labelCheckData) {

    }
}
