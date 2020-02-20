package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.LabelPermission;

import java.util.Optional;
import java.util.Set;

public interface LabelCheckStrategy {
    boolean checkLabelPermissions(Optional<LabelCheckData> labelCheckData, Set<LabelPermission> permissionsToCheck, Account account);
}
