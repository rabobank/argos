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
package com.rabobank.argos.service.adapter.in.rest.user;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestUserProfile;
import com.rabobank.argos.service.domain.security.UserPrincipal;
import com.rabobank.argos.service.domain.user.User;
import com.rabobank.argos.service.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRestServiceImplTest {

    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String KEY_ID = "keyId";
    @Mock
    private UserRepository userRepository;

    private UserRestServiceImpl service;

    @Mock
    private UserPrincipal userPrincipal;

    private User user = User.builder().name(NAME).email(EMAIL).userId(ID).keyIds(List.of(KEY_ID)).build();

    @BeforeEach
    void setUp() {
        service = new UserRestServiceImpl(userRepository);
    }

    @Test
    void getCurrentUser() {
        when(userPrincipal.getId()).thenReturn(ID);
        when(userRepository.findByUserId(ID)).thenReturn(Optional.of(user));
        RestUserProfile user = service.getCurrentUser(userPrincipal);
        assertThat(user.getEmail(), is(EMAIL));
        assertThat(user.getId(), is(ID));
        assertThat(user.getName(), is(NAME));

    }

    @Test
    void getCurrentUserNotFound() {
        when(userPrincipal.getId()).thenReturn(ID);
        when(userRepository.findByUserId(ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getCurrentUser(userPrincipal));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"profile not found for : id\""));
    }

    @Test
    void updateUserProfile() {
        when(userPrincipal.getId()).thenReturn(ID);
        when(userRepository.findByUserId(ID)).thenReturn(Optional.of(user));
        RestUserProfile user = service.updateUserProfile(userPrincipal, new RestUserProfile().id("otherId")
                .email("other_email")
                .name("other_name"));
        assertThat(user.getEmail(), is(EMAIL));
        assertThat(user.getId(), is(ID));
        assertThat(user.getName(), is(NAME));

    }

    @Test
    void updateUserProfileNotFound() {
        when(userPrincipal.getId()).thenReturn(ID);
        when(userRepository.findByUserId(ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateUserProfile(userPrincipal, new RestUserProfile()));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"profile not found for : id\""));
    }
}