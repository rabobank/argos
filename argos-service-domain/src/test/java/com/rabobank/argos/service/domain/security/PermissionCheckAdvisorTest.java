/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.permission.Permission;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionCheckAdvisorTest {

    private static final String ROLE_ID = "roleId";
    private static final String CHECK_BEAN = "checkBean";
    private static final String EXTRACTOR_BEAN = "extraBean";
    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private ApplicationContext applicationContext;

    private PermissionCheckAdvisor advisor;

    @Mock(lenient = true)
    private JoinPoint joinPoint;

    @Mock
    private PermissionCheck permissionCheck;

    @Mock
    private PersonalAccount personalAccount;

    @Mock
    private NonPersonalAccount nonPersonalAccount;

    @Mock
    private MethodSignature signature;

    @Mock
    private LocalPermissionCheckDataExtractor localPermissionCheckDataExtractor;

    @Mock
    private LocalPermissionCheckStrategy localPermissionCheckStrategy;

    @Mock
    private Method method;

    @Mock
    private LocalPermissionCheckData checkData;

    @BeforeEach
    void setUp() {
        advisor = new PermissionCheckAdvisor(accountSecurityContext, applicationContext);
        when(joinPoint.getSignature()).thenReturn(signature);
    }

    @Test
    void checkPermissionsHasGlobalPermission() {
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.READ, Permission.LOCAL_PERMISSION_EDIT));
        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    @Test
    void checkPermissionsHasMultipleGlobalPermission() {
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.READ, Permission.VERIFY});
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.READ, Permission.LOCAL_PERMISSION_EDIT));
        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    @Test
    void checkPermissionsHasWrongGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsHasNoGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsNpaHasNoGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(nonPersonalAccount));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsHasLocalPermission() {
        mockPermissionCheck();
        when(signature.getMethod()).thenReturn(method);
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        Object[] args = new Object[]{};
        when(joinPoint.getArgs()).thenReturn(args);
        when(localPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, args)).thenReturn(checkData);

        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));

        when(localPermissionCheckStrategy.hasLocalPermission(checkData, new HashSet<>(List.of(Permission.LOCAL_PERMISSION_EDIT)))).thenReturn(true);

        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    private void mockPermissionCheck() {
        when(permissionCheck.localPermissionCheckStrategyBean()).thenReturn(CHECK_BEAN);
        when(permissionCheck.localPermissionDataExtractorBean()).thenReturn(EXTRACTOR_BEAN);

        when(applicationContext.getBean(EXTRACTOR_BEAN, LocalPermissionCheckDataExtractor.class)).thenReturn(localPermissionCheckDataExtractor);
        when(applicationContext.getBean(CHECK_BEAN, LocalPermissionCheckStrategy.class)).thenReturn(localPermissionCheckStrategy);
    }
}