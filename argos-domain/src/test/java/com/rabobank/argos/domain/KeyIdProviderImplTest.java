package com.rabobank.argos.domain;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.key.RSAPublicKeyFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class KeyIdProviderImplTest {

    @Test
    void computeKeyId() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] decode = Base64.getDecoder().decode(IOUtils.toByteArray(this.getClass().getResourceAsStream("/publickey.txt")));
        String keyId = new KeyIdProviderImpl().computeKeyId(RSAPublicKeyFactory.instance(decode));
        assertThat(keyId, is("1aaf91a3f8e540cfd9ebbacd6147d43c76abefc535feefd85592197055bea1c8"));
    }
}
