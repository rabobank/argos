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
package com.rabobank.argos.service.adapter.in.rest.permission;

import com.rabobank.argos.domain.permission.GlobalPermission;
import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.adapter.in.rest.api.handler.GlobalPermissionApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestRole;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GlobalPermissionRestService implements GlobalPermissionApi {

    private final RoleRepository roleRepository;
    private final RoleMapper converter;

    @Override
    public ResponseEntity<RestRole> createRole(@Valid RestRole restRole) {
        Role role = converter.convertFromRestRole(restRole);
        roleRepository.save(role);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{roleId}")
                .buildAndExpand(role.getRoleId())
                .toUri();
        return ResponseEntity
                .created(location).body(converter.convertToRestRole(role));
    }

    @PermissionCheck(globalPermissions = {GlobalPermission.READ})
    @Override
    public ResponseEntity<RestRole> getRole(String roleId) {
        return roleRepository.findById(roleId)
                .map(converter::convertToRestRole)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> roleNotFound(roleId));
    }

    @Override
    public ResponseEntity<List<RestRole>> getRoles() {
        List<RestRole> roles = roleRepository.findAll()
                .stream()
                .map(converter::convertToRestRole)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @Override
    public ResponseEntity<RestRole> updateRole(String roleId, @Valid RestRole restRole) {
        return null;
    }

    private static ResponseStatusException roleNotFound(String roleId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found : " + roleId);
    }
}
