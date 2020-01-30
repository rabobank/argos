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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TokenAuthenticationFilter filter;

    @Mock
    private UserDetails user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(null);
        filter = new TokenAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    @Test
    void doFilterInternal() throws ServletException, IOException {
        when(tokenProvider.getUserIdFromToken("jwtToken")).thenReturn("id");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(customUserDetailsService.loadUserById("id")).thenReturn(user);
        when(tokenProvider.validateToken("jwtToken")).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getDetails() != null, is(true));
        assertThat(authentication.getPrincipal(), sameInstance(user));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalNotValidJwt() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication() == null, is(true));
    }

    @Test
    void doFilterInternalWrongBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("bearer jwtToken");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication() == null, is(true));
    }

    @Test
    void doFilterInternalNoAuthorization() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication() == null, is(true));
    }
}