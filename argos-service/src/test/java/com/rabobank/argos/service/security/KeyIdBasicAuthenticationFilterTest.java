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
package com.rabobank.argos.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyIdBasicAuthenticationFilterTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private BasicAuthenticationConverter basicAuthenticationConverter;

    private KeyIdBasicAuthenticationFilter keyIdBasicAuthenticationFilter;

    private UsernamePasswordAuthenticationToken basicAuthPwToken = new UsernamePasswordAuthenticationToken("keyId", "pw", Collections.emptyList());

    @BeforeEach
    void setup() {
        keyIdBasicAuthenticationFilter = new KeyIdBasicAuthenticationFilter(basicAuthenticationConverter);
    }

    @Test
    void doFilterInternalWithValidBasicHeader() throws IOException, ServletException {
        when(basicAuthenticationConverter.convert(request))
                .thenReturn(basicAuthPwToken);
        keyIdBasicAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, instanceOf(NonPersonalAccountAuthenticationToken.class));
        NonPersonalAccountAuthenticationToken nonPersonalAccountAuthenticationToken = (NonPersonalAccountAuthenticationToken) authentication;
        assertThat(nonPersonalAccountAuthenticationToken.getNonPersonalAccountCredentials().getKeyId(), is("keyId"));
        assertThat(nonPersonalAccountAuthenticationToken.getNonPersonalAccountCredentials().getPassword(), is("pw"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalWithInvalidValidBasicHeader() throws IOException, ServletException {
        when(basicAuthenticationConverter.convert(request))
                .thenReturn(null);
        keyIdBasicAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, nullValue());
        verify(filterChain).doFilter(request, response);
    }
}