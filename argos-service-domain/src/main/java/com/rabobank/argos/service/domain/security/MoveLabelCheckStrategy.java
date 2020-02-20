package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.LabelPermission;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@Qualifier("moveLabelCheckStrategy")
@RequiredArgsConstructor
public class MoveLabelCheckStrategy implements LabelCheckStrategy {

    private final HierarchyRepository hierarchyRepository;

    @Override
    public boolean checkLabelPermissions(Optional<LabelCheckData> labelCheckData, Set<LabelPermission> permissionsToCheck, Account account) {
        return true;
    }
}
