package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionCheckAdvisor {

    private final AccountSecurityContext accountSecurityContext;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    @Pointcut("@annotation(permissionCheck)")
    public void permissionCheckPointCut(PermissionCheck permissionCheck) {
        //This is an AspectJ pointcut implemented as method
    }

    @Before(value = "permissionCheckPointCut(permissionCheck)", argNames = "joinPoint,permissionCheck")
    public void checkPermissions(JoinPoint joinPoint, PermissionCheck permissionCheck) {
        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow();

        log.info("checking permissions of method:{} for account: {} with global permissions: {} and label permissions :{}",
                joinPoint.getSignature().getName(),
                account.getName(),
                permissionCheck.globalPermissions(),
                permissionCheck.labelPermissions()
        );

        checkLabelPermissions(joinPoint, permissionCheck, account);

    }

    private void checkLabelPermissions(JoinPoint joinPoint, PermissionCheck permissionCheck, Account account) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] argumentValues = joinPoint.getArgs();

        LabelCheckDataExtractor labelCheckDataExtractor = applicationContext
                .getBean(permissionCheck.labelPermissionDataExtractorBean(), LabelCheckDataExtractor.class);

        LabelCheckStrategy labelCheckStrategy = applicationContext.getBean(permissionCheck
                .labelCheckStrategyBean(), LabelCheckStrategy.class);

        Optional<LabelCheckData> labelCheckData = labelCheckDataExtractor.extractLabelCheckData(method, argumentValues);
        labelCheckStrategy.checkLabelPermissions(labelCheckData,
                new HashSet<>(List.of(permissionCheck.labelPermissions())),
                account);
    }
}
