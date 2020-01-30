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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private OAuth2AuthenticationFailureHandler failureHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    @BeforeEach
    void setUp() {
        failureHandler = new OAuth2AuthenticationFailureHandler(httpCookieOAuth2AuthorizationRequestRepository);
    }

    @Test
    void onAuthenticationFailure() throws IOException {
        when(httpCookieOAuth2AuthorizationRequestRepository.getRedirectUri(request)).thenReturn(Optional.of("https://host:89/uri"));
        when(exception.getLocalizedMessage()).thenReturn("message");
        failureHandler.onAuthenticationFailure(request, response, exception);
        verify(response).encodeRedirectURL("https://host:89/uri?error=message");
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }
}