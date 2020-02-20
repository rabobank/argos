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
package com.rabobank.argos.service.adapter.in.rest.hierarchy;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestLabel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestLabelTest {


    @Test
    void emptyRestLabel() {
        assertThat(validate(new RestLabel()), contains(expectedErrors(
                "name", "must not be null"
        )));
    }

    @Test
    void invalidRestSupplyChain() {
        assertThat(validate(new RestLabel().name("Invalid").parentLabelId("wrong")), contains(expectedErrors(
                "name", "must match \"^([a-z]{1}[a-z0-9_]*)?$\"",
                "parentLabelId", "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}?$\"",
                "parentLabelId", "size must be between 36 and 36"
        )));
    }

    @Test
    void validRestSupplyChain() {
        assertThat(validate(new RestLabel().name("valid_1").parentLabelId(UUID.randomUUID().toString())), empty());
    }

}