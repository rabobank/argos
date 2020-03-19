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
package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class DefaultTestData {
    private String adminToken;
    private Map<String, PersonalAccount> personalAccounts = new HashMap<>();
    private RestLabel defaultRootLabel;
    private Map<String, NonPersonalAccount> nonPersonalAccount = new HashMap<>();

    @Builder
    @Getter
    @Setter
    public static class PersonalAccount {
        private String token;
        private String keyId;
        private String passphrase;
        private byte[] publicKey;
    }

    @Builder
    @Getter
    @Setter
    public static class NonPersonalAccount {
        private String keyId;
        private String passphrase;
        private String hashedKeyPassphrase;
        private byte[] publicKey;
    }

}

