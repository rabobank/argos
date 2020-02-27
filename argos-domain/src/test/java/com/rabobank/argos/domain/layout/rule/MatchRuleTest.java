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
package com.rabobank.argos.domain.layout.rule;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabobank.argos.domain.layout.ArtifactType;

class MatchRuleTest {

    @Test
    void nonNullTest() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            MatchRule rule = new MatchRule(null, null, ArtifactType.MATERIALS, null, null, null); 
          });
        
        assertEquals("pattern is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            MatchRule rule = new MatchRule("", null, null, null, null, null); 
          });
        
        assertEquals("destinationType is marked non-null but is null", exception.getMessage());
        
    }

}
