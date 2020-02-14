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
package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import org.junit.jupiter.api.Test;

import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestVerifyCommandTest {


    @Test
    void emptyRestVerifyCommand() {
        assertThat(validate(new RestVerifyCommand()), contains(expectedErrors(
                "expectedProducts", "size must be between 1 and 2147483647"
        )));
    }

    @Test
    void emptyRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact())), contains(expectedErrors(
                "expectedProducts[0].hash", "must not be null",
                "expectedProducts[0].uri", "must not be null"
        )));
    }

    @Test
    void invalidRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact().hash("hash").uri("\t\t\t\\wrong"))), contains(expectedErrors(
                "expectedProducts[0].hash", "must match \"^[0-9a-f]*$\"",
                "expectedProducts[0].hash", "size must be between 64 and 64",
                "expectedProducts[0].uri", "must match \"^(?!.*\\\\).*$\""
        )));
    }

    @Test
    void validRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact().hash("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254").uri("/test.jar"))), empty());
    }

}