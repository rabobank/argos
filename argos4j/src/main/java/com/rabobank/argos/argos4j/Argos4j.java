package com.rabobank.argos.argos4j;

import com.rabobank.argos.argos4j.internal.Argos4JSigner;
import com.rabobank.argos.argos4j.internal.ArgosServiceClient;
import com.rabobank.argos.argos4j.internal.ArtifactCollector;
import com.rabobank.argos.argos4j.internal.JsonSigningSerializer;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Signature;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.List;

import static java.util.Collections.singletonList;

@AllArgsConstructor
public class Argos4j {

    private final Argos4jSettings settings;


    public void storeMetablockLinkForDirectory(File materialsDirectory, File productsDirectory) {
        List<Artifact> materials = new ArtifactCollector(settings, materialsDirectory.getPath()).collect("");
        List<Artifact> products = new ArtifactCollector(settings, productsDirectory.getPath()).collect("");
        ;
        Link link = Link.builder().materials(materials).products(products).stepName(settings.getStepName()).build();
        Signature signature = new Argos4JSigner().sign(settings.getSigningKey(), new JsonSigningSerializer().serialize(link));
        new ArgosServiceClient(settings).uploadLinkMetaBlockToService(LinkMetaBlock.builder().link(link).signatures(singletonList(signature)).build());
    }

}
