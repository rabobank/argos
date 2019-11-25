package com.rabobank.argos.argos4j;

import com.rabobank.argos.argos4j.internal.Argos4JSigner;
import com.rabobank.argos.argos4j.internal.ArgosServiceClient;
import com.rabobank.argos.argos4j.internal.ArtifactCollector;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Signature;
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
        Link link = Link.builder().runId(settings.getRunId()).materials(materials).products(products).stepName(settings.getStepName()).build();
        Signature signature = new Argos4JSigner().sign(settings.getSigningKey(), new JsonSigningSerializer().serialize(link));
        new ArgosServiceClient(settings).uploadLinkMetaBlockToService(LinkMetaBlock.builder().link(link).signature(signature).build());
    }

}
