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

import com.rabobank.argos.service.security.CookieHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Mock
    private CookieHelper cookieHelper;

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2AuthorizationRequest oAuth2AuthorizationRequest;

    @Mock
    private Cookie cookie;

    @BeforeEach
    void setUp() {
        oAuth2AuthorizationRequest = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("http://some").clientId("is").build();
        repository = new HttpCookieOAuth2AuthorizationRequestRepository(cookieHelper);
    }

    @Test
    void loadAuthorizationRequestFound() {
        when(cookieHelper.getCookieValueAsObject(request, "oauth2_auth_request", OAuth2AuthorizationRequest.class)).thenReturn(Optional.of(oAuth2AuthorizationRequest));
        assertThat(repository.loadAuthorizationRequest(request), sameInstance(oAuth2AuthorizationRequest));
    }

    @Test
    void loadAuthorizationRequestNotFound() {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = repository.loadAuthorizationRequest(request);
        assertThat(oAuth2AuthorizationRequest == null, is(true));
    }

    @Test
    void saveAuthorizationRequestNull() {
        repository.saveAuthorizationRequest(null, request, response);
        verify(cookieHelper).deleteCookie(request, response, "oauth2_auth_request");
        verify(cookieHelper).deleteCookie(request, response, "redirect_uri");
    }

    @Test
    void saveAuthorizationRequestNotNull() {
        when(request.getParameter("redirect_uri")).thenReturn("url");
        repository.saveAuthorizationRequest(oAuth2AuthorizationRequest, request, response);
        verify(cookieHelper).addCookie(response, "oauth2_auth_request", oAuth2AuthorizationRequest, 180);
        verify(cookieHelper).addCookie(response, "redirect_uri", "url", 180);
    }

    @Test
    void removeAuthorizationRequest() {
        when(cookieHelper.getCookieValueAsObject(request, "oauth2_auth_request", OAuth2AuthorizationRequest.class)).thenReturn(Optional.of(oAuth2AuthorizationRequest));
        assertThat(repository.removeAuthorizationRequest(request), sameInstance(oAuth2AuthorizationRequest));
    }


    @Test
    void getRedirectUri() {
        when(cookie.getValue()).thenReturn("value");
        when(cookieHelper.getCookie(request, "redirect_uri")).thenReturn(Optional.of(cookie));
        assertThat(repository.getRedirectUri(request), is(Optional.of("value")));
    }
}