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
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.domain.link.Artifact;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RemoteArtifactCollector implements ArtifactCollector {

    private final FileCollector fileCollector;
    private final ZipStreamArtifactCollector zipStreamArtifactCollector;

    public RemoteArtifactCollector(FileCollector fileCollector) {
        this.fileCollector = fileCollector;
        zipStreamArtifactCollector = new ZipStreamArtifactCollector(fileCollector);
    }

    @Override
    public List<Artifact> collect() {
        Client client = new Client.Default(null, null);
        URI uri = fileCollector.getUri();
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        try {
            requestTemplate.target(uri.toURL().toString());
        } catch (MalformedURLException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
        Optional.ofNullable(uri.getUserInfo())
                .map(userInfo -> userInfo.split(":"))
                .filter(userInfo -> userInfo.length == 2)
                .ifPresent(userInfo -> new BasicAuthRequestInterceptor(userInfo[0], userInfo[1]).apply(requestTemplate));

        try (Response response = client.execute(requestTemplate.resolve(new HashMap<>()).request(), new Request.Options())) {
            if (response.status() == 200) {
                return zipStreamArtifactCollector.collect(response.body().asInputStream());
            } else {
                throw new Argos4jError("status code : " + response.status() + " returned");
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}
