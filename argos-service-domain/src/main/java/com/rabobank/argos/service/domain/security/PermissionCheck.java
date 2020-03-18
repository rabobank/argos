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

import com.rabobank.argos.domain.permission.Permission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.rabobank.argos.service.domain.security.DefaultLocalPermissionCheckDataExtractor.DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;
import static com.rabobank.argos.service.domain.security.DefaultLocalPermissionCheckStrategy.DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionCheck {

    Permission[] permissions() default {};

    String localPermissionDataExtractorBean() default DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;

    String localPermissionCheckStrategyBean() default DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;

}