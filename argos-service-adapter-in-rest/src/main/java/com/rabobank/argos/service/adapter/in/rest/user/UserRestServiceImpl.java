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
import com.rabobank.argos.service.domain.security.CurrentUser;
import com.rabobank.argos.service.domain.security.UserPrincipal;
import com.rabobank.argos.service.domain.user.User;
import com.rabobank.argos.service.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserRestServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public RestUserProfile getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findByUserId(userPrincipal.getId()).map(this::convert)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "profile not found for : " + userPrincipal.getId()));
    }

    @PutMapping(value = "/user/me")
    @PreAuthorize("hasRole('USER')")
    public RestUserProfile updateUserProfile(@CurrentUser UserPrincipal userPrincipal, @Valid @RequestBody RestUserProfile restUserProfile) {
        return userRepository.findByUserId(userPrincipal.getId()).map(user -> {
            user.setKeyIds(restUserProfile.getKeyIds());
            userRepository.update(user);
            return convert(user);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private RestUserProfile convert(User user) {
        return new RestUserProfile().id(user.getUserId()).name(user.getName()).email(user.getEmail()).keyIds(user.getKeyIds());
    }

}
