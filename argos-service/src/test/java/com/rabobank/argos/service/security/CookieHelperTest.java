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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CookieHelperTest {

    private CookieHelper cookieHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Cookie cookie;

    @Mock
    private HttpServletResponse response;

    @Captor
    private ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @BeforeEach
    void setUp() {
        cookieHelper = new CookieHelper();
    }

    @Test
    void getCookie() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        assertThat(cookieHelper.getCookie(request, "name"), is(Optional.of(cookie)));
    }

    @Test
    void getCookieNotFound() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        assertThat(cookieHelper.getCookie(request, "other"), is(Optional.empty()));
    }

    @Test
    void addCookie() {
        cookieHelper.addCookie(response, "name", "value", 12);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();
        assertThat(cookie.getName(), is("name"));
        assertThat(cookie.getValue(), is("value"));
        assertThat(cookie.getPath(), is("/"));
        assertThat(cookie.getMaxAge(), is(12));
        assertThat(cookie.isHttpOnly(), is(true));
    }

    @Test
    void deleteCookie() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        cookieHelper.deleteCookie(request, response, "name");
        verify(cookie).setMaxAge(0);
        verify(cookie).setValue("");
        verify(cookie).setPath("/");
    }

    @Test
    void addCookieObject() {
        Cookie object = new Cookie("name", "value");

        cookieHelper.addCookie(response, "name", object, 12);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();
        assertThat(cookie.getName(), is("name"));
        assertThat(cookie.getValue(), is("rO0ABXNyABlqYXZheC5zZXJ2bGV0Lmh0dHAuQ29va2llAAAAAAAAAAECAAlaAAhodHRwT25seUkABm1heEFnZVoABnNlY3VyZUkAB3ZlcnNpb25MAAdjb21tZW50dAASTGphdmEvbGFuZy9TdHJpbmc7TAAGZG9tYWlucQB-AAFMAARuYW1lcQB-AAFMAARwYXRocQB-AAFMAAV2YWx1ZXEAfgABeHAA_____wAAAAAAcHB0AARuYW1lcHQABXZhbHVl"));
        assertThat(cookie.getMaxAge(), is(12));
        assertThat(cookie.isHttpOnly(), is(true));
    }

    @Test
    void getCookieObject() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getValue()).thenReturn("rO0ABXNyABlqYXZheC5zZXJ2bGV0Lmh0dHAuQ29va2llAAAAAAAAAAECAAlaAAhodHRwT25seUkABm1heEFnZVoABnNlY3VyZUkAB3ZlcnNpb25MAAdjb21tZW50dAASTGphdmEvbGFuZy9TdHJpbmc7TAAGZG9tYWlucQB-AAFMAARuYW1lcQB-AAFMAARwYXRocQB-AAFMAAV2YWx1ZXEAfgABeHAA_____wAAAAAAcHB0AARuYW1lcHQABXZhbHVl");

        Optional<Cookie> optionalCookie = cookieHelper.getCookieValueAsObject(request, "name", Cookie.class);
        assertThat(optionalCookie.get().getValue(), is("value"));
        assertThat(optionalCookie.get().getName(), is("name"));
    }
}