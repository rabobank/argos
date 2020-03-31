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
package com.rabobank.argos.domain.account;

import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@ExtendWith(MockitoExtension.class)
class NonPersonalAccountTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String NAME = "name";

    @Mock
    private NonPersonalAccountKeyPair activeKeyPair;

    @Mock
    private NonPersonalAccountKeyPair keyPair;

    @Test
    void builder() {
        NonPersonalAccount account = NonPersonalAccount.builder().name(NAME)
                .parentLabelId(PARENT_LABEL_ID)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singletonList(keyPair))
                .build();
        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getEmail(), nullValue());
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getParentLabelId(), is(PARENT_LABEL_ID));
        assertThat(account.getLocalPermissions(), contains(LocalPermissions.builder().labelId(PARENT_LABEL_ID).permissions(
                Arrays.asList(Permission.LINK_ADD, Permission.VERIFY)).build()));
    }
}