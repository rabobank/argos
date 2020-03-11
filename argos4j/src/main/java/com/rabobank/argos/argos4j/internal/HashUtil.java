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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.UnixLineEndingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HashUtil {

    public static String createHash(InputStream inputStream, String filename, boolean normalizeLineEndings) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        try {
            InputStream file = normalizeLineEndings ? new UnixLineEndingInputStream(inputStream, false) : inputStream;
            byte[] buffer = new byte[2048];
            int len;
            while ((len = file.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        return Hex.encodeHexString(digest.digest());
    }
}
