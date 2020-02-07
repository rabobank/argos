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
package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleVerificationContextTest {
    

    
    @Mock
    private ArtifactsVerificationContext artifactsContext;

    private Rule rule = new Rule(RuleType.ALLOW, "someDir/*.jar");
    
    @Test
    void nonNull() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            RuleVerificationContext.builder()
            .artifactsContext(artifactsContext)
            .rule(null)
            .build(); 
          });
        assertEquals("rule is marked non-null but is null", exception.getMessage());
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            RuleVerificationContext.builder()
            .artifactsContext(null)
            .rule(rule)
            .build(); 
          });
        assertEquals("artifactsContext is marked non-null but is null", exception.getMessage());
    }
}
