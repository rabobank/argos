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
import com.rabobank.argos.service.domain.account.AuthenticationProvider;
import com.rabobank.argos.service.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.security.oauth2.user.OAuth2UserInfo;
import com.rabobank.argos.service.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final PersonalAccountRepository personalAccountRepository;

    private DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        try {
            OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(oAuth2UserRequest);
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private ArgosOAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        AuthenticationProvider authenticationProvider = AuthenticationProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authenticationProvider, oAuth2User.getAttributes());
        if (!StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            Optional<PersonalAccount> userOptional = personalAccountRepository.findByEmail(oAuth2UserInfo.getEmail());
            if (userOptional.isPresent()) {
                return new ArgosOAuth2User(oAuth2User, updateExistingUser(userOptional.get(), oAuth2UserInfo));
            } else {
                return new ArgosOAuth2User(oAuth2User, registerNewUser(authenticationProvider, oAuth2UserInfo));
            }
        } else {
            throw new ArgosError("email address not provided by oauth profile service");
        }
    }

    private String registerNewUser(AuthenticationProvider authenticationProvider, OAuth2UserInfo oAuth2UserInfo) {
        PersonalAccount personalAccount = PersonalAccount.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .providerId(oAuth2UserInfo.getId())
                .provider(authenticationProvider)
                .build();
        personalAccountRepository.save(personalAccount);
        return personalAccount.getAccountId();
    }

    private String updateExistingUser(PersonalAccount existingPersonalAccount, OAuth2UserInfo oAuth2UserInfo) {
        existingPersonalAccount.setName(oAuth2UserInfo.getName());
        personalAccountRepository.update(existingPersonalAccount);
        return existingPersonalAccount.getAccountId();
    }

}
