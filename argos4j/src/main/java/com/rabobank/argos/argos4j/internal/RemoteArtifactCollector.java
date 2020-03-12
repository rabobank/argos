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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.argos4j.FileCollector.FileCollectorType.REMOTE_ZIP;

@Slf4j
public class RemoteArtifactCollector implements ArtifactCollector {

    private final FileCollector fileCollector;

    public RemoteArtifactCollector(FileCollector fileCollector) {
        this.fileCollector = fileCollector;
    }

    @Override
    public List<Artifact> collect() {
        URI uri = fileCollector.getUri();
        RequestTemplate requestTemplate = createRequest(uri);
        Client client = new Client.Default(null, null);
        Request request = requestTemplate.resolve(new HashMap<>()).request();
        log.info("execute request: {}", request.url());
        try (Response response = client.execute(request, new Request.Options())) {
            if (response.status() == 200) {
                return getArtifactsFromResponse(uri, response);
            } else {
                throw new Argos4jError("status code : " + response.status() + " returned");
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }

    private List<Artifact> getArtifactsFromResponse(URI uri, Response response) throws IOException {
        if (fileCollector.getType() == REMOTE_ZIP) {
            return new ZipStreamArtifactCollector(fileCollector).collect(response.body().asInputStream());
        } else {
            String fileName = Optional.ofNullable(fileCollector.getSettings().getArtifactUri())
                    .orElseGet(() -> uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
            String hash = HashUtil.createHash(response.body().asInputStream(), fileName, fileCollector.getSettings().isNormalizeLineEndings());
            return Collections.singletonList(Artifact.builder().hash(hash).uri(fileName).build());
        }
    }

    private RequestTemplate createRequest(URI uri) {
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        try {
            requestTemplate.target(uri.toURL().toString());
        } catch (MalformedURLException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
        addAuthorization(uri, requestTemplate);
        return requestTemplate;
    }

    private void addAuthorization(URI uri, RequestTemplate requestTemplate) {
        Optional.ofNullable(uri.getUserInfo())
                .map(userInfo -> userInfo.split(":"))
                .filter(userInfo -> userInfo.length == 2)
                .ifPresent(userInfo -> new BasicAuthRequestInterceptor(userInfo[0], userInfo[1]).apply(requestTemplate));
    }
}
