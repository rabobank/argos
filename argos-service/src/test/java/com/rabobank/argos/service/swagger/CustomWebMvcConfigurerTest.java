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
package com.rabobank.argos.service.swagger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomWebMvcConfigurerTest {

    private CustomWebMvcConfigurer configurer;

    @Mock
    private ViewControllerRegistry registry;

    @Mock
    private ViewControllerRegistration viewControllerRegistration;

    @BeforeEach
    void setUp() {
        configurer = new CustomWebMvcConfigurer();
    }

    @Test
    void addViewControllers() {
        when(registry.addViewController(anyString())).thenReturn(viewControllerRegistration);
        configurer.addViewControllers(registry);

        verify(registry).addViewController("/swagger");
        verify(registry).addViewController("/swagger/");

        verify(viewControllerRegistration).setViewName("redirect:/swagger/");
        verify(viewControllerRegistration).setViewName("forward:/swagger/index.html");
    }
}