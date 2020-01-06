/*
 * Copyright (C) 2019 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

class VerificationResultMapperTest {

    VerificationResultMapper verificationResultMapper;

    @BeforeEach
    public void setup() {
        verificationResultMapper = Mappers.getMapper(VerificationResultMapper.class);
    }

    @Test
    void mapToRestVerificationResultShouldReturnResult() {
        VerificationRunResult restVerificationResult = VerificationRunResult.okay();
        RestVerificationResult result = verificationResultMapper.mapToRestVerificationResult(restVerificationResult);
        assertThat(result.getRunIsValid(), is(true));
    }
}