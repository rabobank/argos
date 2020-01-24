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

import com.rabobank.argos.service.domain.user.AuthenticationProvider;
import com.rabobank.argos.service.domain.user.User;
import com.rabobank.argos.service.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;


    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService userService;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        clientRegistration = ClientRegistration.withRegistrationId("azure")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("clientId")
                .redirectUriTemplate("template")
                .authorizationUri("/")
                .tokenUri("/")
                .build();
        userService = new CustomOAuth2UserService(userRepository);
        ReflectionTestUtils.setField(userService, "defaultOAuth2UserService", defaultOAuth2UserService);
    }

    @Test
    void loadUserNewUser() {
        setupMocks();
        when(userRepository.findByEmail("userprincipalname")).thenReturn(Optional.empty());
        ArgosOAuth2User userPrincipal = (ArgosOAuth2User) userService.loadUser(oAuth2UserRequest);
        assertThat(userPrincipal.getUserId(), hasLength(36));

        verify(userRepository).save(userArgumentCaptor.capture());
        User createdUser = userArgumentCaptor.getValue();
        assertThat(createdUser.getName(), is("displayName"));
        assertThat(createdUser.getEmail(), is("userprincipalname"));
        assertThat(createdUser.getProvider(), is(AuthenticationProvider.AZURE));
        assertThat(createdUser.getProviderId(), is("providerId"));
    }

    @Test
    void loadUserExistingUser() {
        setupMocks();
        when(user.getUserId()).thenReturn("userId");
        when(userRepository.findByEmail("userprincipalname")).thenReturn(Optional.of(user));
        ArgosOAuth2User userPrincipal = (ArgosOAuth2User) userService.loadUser(oAuth2UserRequest);
        assertThat(userPrincipal.getUserId(), is("userId"));

        verify(userRepository).update(user);
        verify(user).setName("displayName");
    }

    @Test
    void loadUserNoEmailAddress() {
        setupMocks();
        when(oAuth2User.getAttributes()).thenReturn(Map.of("displayName", "displayName", "id", "providerId"));
        InternalAuthenticationServiceException serviceException = assertThrows(InternalAuthenticationServiceException.class, () -> userService.loadUser(oAuth2UserRequest));
        assertThat(serviceException.getCause().getMessage(), is("email address not provided by oauth profile service"));
    }

    private void setupMocks() {
        when(oAuth2User.getAttributes()).thenReturn(Map.of("userPrincipalName", "userPrincipalName", "displayName", "displayName", "id", "providerId"));
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
    }
}