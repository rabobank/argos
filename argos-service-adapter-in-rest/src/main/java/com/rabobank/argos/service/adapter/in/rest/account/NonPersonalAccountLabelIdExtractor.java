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
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.service.domain.account.NonPersonalAccountRepository;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.LabelIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component(NonPersonalAccountLabelIdExtractor.NPA_LABEL_ID_EXTRACTOR)
@RequiredArgsConstructor
public class NonPersonalAccountLabelIdExtractor implements LabelIdExtractor {
    public static final String NPA_LABEL_ID_EXTRACTOR = "NonPersonalAccountLabelIdExtractor";

    private final NonPersonalAccountRepository nonPersonalAccountRepository;

    @Override
    public Optional<String> extractLabelId(LabelIdCheckParam checkParam, Object accountId) {
        return nonPersonalAccountRepository.findParentLabelIdByAccountId((String) accountId);
    }
}
