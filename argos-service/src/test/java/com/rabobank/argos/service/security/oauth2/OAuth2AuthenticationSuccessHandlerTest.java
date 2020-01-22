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
package com.rabobank.argos.service.security.oauth2;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.service.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;


    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(tokenProvider, httpCookieOAuth2AuthorizationRequestRepository);
        ReflectionTestUtils.setField(successHandler, "authorizedRedirectUris", List.of(URI.create("https://host:89/uri")));
    }

    @Test
    void parseUris() {
        List<URI> uris = OAuth2AuthenticationSuccessHandler.parseUris(new String[]{"https://localhost:534/hiero"});
        assertThat(uris, contains(URI.create("https://localhost:534/hiero")));
    }

    @Test
    void onAuthenticationSuccess() throws IOException {
        when(tokenProvider.createToken(authentication)).thenReturn("token");
        when(httpCookieOAuth2AuthorizationRequestRepository.getRedirectUri(request)).thenReturn(Optional.of("https://host:89/uri"));
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(response).encodeRedirectURL("https://host:89/uri?token=token");
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccessWrongRedirectUrl() {
        when(httpCookieOAuth2AuthorizationRequestRepository.getRedirectUri(request)).thenReturn(Optional.of("https://host/uri"));
        ArgosError argosError = assertThrows(ArgosError.class, () -> successHandler.onAuthenticationSuccess(request, response, authentication));
        assertThat(argosError.getMessage(), is("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication"));
    }

    @Test
    void onAuthenticationIsCommitted() throws IOException {
        when(response.isCommitted()).thenReturn(true);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verifyNoMoreInteractions(response);
    }
}