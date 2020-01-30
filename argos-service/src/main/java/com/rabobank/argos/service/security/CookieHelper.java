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

import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class CookieHelper {

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies ->
                        Stream.of(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst());
    }

    public <T> Optional<T> getCookieValueAsObject(HttpServletRequest request, String name, Class<T> cls) {
        return getCookie(request, name).map(Cookie::getValue).map(value -> deserialize(value, cls));
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public void addCookie(HttpServletResponse response, String name, Object object, int maxAge) {
        addCookie(response, name, serialize(object), maxAge);
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        getCookie(request, name).ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
    }

    private String serialize(Object object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    private <T> T deserialize(String objectAsString, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(objectAsString)));
    }

}
