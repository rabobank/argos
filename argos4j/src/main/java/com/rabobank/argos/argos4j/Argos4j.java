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
package com.rabobank.argos.argos4j;

import com.rabobank.argos.argos4j.internal.Argos4JSigner;
import com.rabobank.argos.argos4j.internal.ArgosServiceClient;
import com.rabobank.argos.argos4j.internal.ArtifactCollector;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.JsonSigningSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

@RequiredArgsConstructor
public class Argos4j implements Serializable {

    @Getter
    private final Argos4jSettings settings;

    private ArrayList<Artifact> materials = new ArrayList<>();
    private ArrayList<Artifact> products = new ArrayList<>();

    public void collectMaterials(File materialsDirectory) {
        materials.addAll(new ArtifactCollector(settings, materialsDirectory.getPath()).collect(""));
    }

    public void collectProducts(File productsDirectory) {
        products.addAll(new ArtifactCollector(settings, productsDirectory.getPath()).collect(""));
    }

    public void store() {
        Link link = Link.builder().runId(settings.getRunId())
                .materials(materials)
                .products(products)
                .layoutSegmentName(settings.getLayoutSegmentName())
                .stepName(settings.getStepName()).build();
        Signature signature = new Argos4JSigner().sign(settings.getSigningKey(), new JsonSigningSerializer().serialize(link));
        new ArgosServiceClient(settings).uploadLinkMetaBlockToService(LinkMetaBlock.builder().link(link).signature(signature).build());
    }

    public static String getVersion() {
        return VersionInfo.getInfo();
    }
}
