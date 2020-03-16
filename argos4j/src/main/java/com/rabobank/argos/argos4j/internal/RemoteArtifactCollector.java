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
import com.rabobank.argos.argos4j.RemoteCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector;
import com.rabobank.argos.argos4j.RemoteZipFileCollector;
import com.rabobank.argos.domain.link.Artifact;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RemoteArtifactCollector implements ArtifactCollector {

    private final RemoteCollector remoteCollector;

    public RemoteArtifactCollector(RemoteFileCollector remoteCollector) {
        this.remoteCollector = remoteCollector;
    }

    public RemoteArtifactCollector(RemoteZipFileCollector remoteCollector) {
        this.remoteCollector = remoteCollector;
    }

    @Override
    public List<Artifact> collect() {
        RequestTemplate requestTemplate = createRequest();
        Client client = new Client.Default(null, null);
        Request request = requestTemplate.resolve(new HashMap<>()).request();
        log.info("execute request: {}", request.url());
        try (Response response = client.execute(request, new Request.Options())) {
            if (response.status() == 200) {
                return getArtifactsFromResponse(response);
            } else {
                String bodyAsString = Optional.ofNullable(response.body()).map(this::convert).filter(body -> !body.isEmpty()).map(body -> " with body : " + body).orElse("");
                throw new Argos4jError("call to " + request.url() + " returned " + response.status() + bodyAsString);
            }
        } catch (IOException e) {
            throw new Argos4jError(request.url() + " got error " + e.getMessage(), e);
        }
    }

    public String convert(Response.Body body) {
        try (BufferedReader br = new BufferedReader(body.asReader(UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private List<Artifact> getArtifactsFromResponse(Response response) throws IOException {
        if (remoteCollector.getClass() == RemoteZipFileCollector.class) {
            return new ZipStreamArtifactCollector(remoteCollector).collect(response.body().asInputStream());
        } else if (remoteCollector.getClass() == RemoteFileCollector.class) {
            String fileName = Optional.ofNullable(((RemoteFileCollector) remoteCollector).getArtifactUri())
                    .orElseGet(() -> remoteCollector.getUrl().getPath().substring(remoteCollector.getUrl().getPath().lastIndexOf('/') + 1));
            String hash = HashUtil.createHash(response.body().asInputStream(), fileName, remoteCollector.isNormalizeLineEndings());
            return Collections.singletonList(Artifact.builder().hash(hash).uri(fileName).build());
        } else {
            throw new Argos4jError("not implemented");
        }
    }

    private RequestTemplate createRequest() {
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        requestTemplate.target(remoteCollector.getUrl().toString());
        addAuthorization(requestTemplate);
        return requestTemplate;
    }


    private void addAuthorization(RequestTemplate requestTemplate) {
        Optional.ofNullable(remoteCollector.getUsername())
                .ifPresent(userInfo -> new BasicAuthRequestInterceptor(remoteCollector.getUsername(),
                        new String(remoteCollector.getPassword())).apply(requestTemplate));
    }
}
