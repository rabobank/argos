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