package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.LabelPermission;

import java.lang.reflect.Method;
import java.util.Set;

public interface LabelCheckDataExtractor {
    LabelCheckData extractLabelCheckData(Method method, Set<LabelPermission> permissions, Account account);
}
