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
package com.rabobank.argos.argos4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabobank.argos.argos4j.internal.ArgosServiceClient;

class ArgosServiceClientTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void calculatePassphraseTest() {
        String expected = "de38d63d28c4478625b22bbd2ef43447ee12e51d68e6fa121413b4052fb29a434126e439ab3ab96790109d04054ec6d82e95e72d551f78b7bdf674f90191ea34";
        String keyId = "ef07177be75d393163c1589f6dce3c41dd7d4ac4a0cbe4777d2aa45b53342dc6";
        String passphrase = "R5gkNnqKdBM9eF";
        
        assertEquals(expected, ArgosServiceClient.calculatePassphrase(keyId, passphrase));
    }

}
