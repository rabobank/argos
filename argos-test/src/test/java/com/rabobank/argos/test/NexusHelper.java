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
package com.rabobank.argos.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class NexusHelper {
    
    private static Properties properties = Properties.getInstance();

    static String getWarSnapshotHash() {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getNexusWarSnapshotUrl()))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assertThat(send.statusCode(), is(200));
            MessageDigest digest = DigestUtils.getSha256Digest();
            digest.update(send.body(), 0, send.body().length);
            return Hex.encodeHexString(digest.digest());
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        return null;
    }
    
    public static String getDarSnapshot() {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getNexusDarSnapshotUrl()))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assertThat(send.statusCode(), is(200));
            MessageDigest digest = DigestUtils.getSha256Digest();
            digest.update(send.body(), 0, send.body().length);
            return Hex.encodeHexString(digest.digest());
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        return null;
    }

}
