package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.permission.GlobalPermission;
import com.rabobank.argos.domain.permission.LabelPermission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionCheck {
    GlobalPermission[] globalPermissions() default {};

    LabelPermission[] labelPermissions() default {};

    String labelPermissionDataExtractorBean() default "defaultLabelCheckDataExtractor";

    String labelCheckStrategyBean() default "defaultLabelCheckStrategy";

}