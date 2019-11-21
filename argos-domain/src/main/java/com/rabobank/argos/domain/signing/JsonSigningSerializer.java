package com.rabobank.argos.domain.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.Step;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;

public class JsonSigningSerializer implements SigningSerializer {

    @Override
    public String serialize(Link link) {
        Link linkClone = Mappers.getMapper(Cloner.class).clone(link);
        linkClone.getMaterials().sort(Comparator.comparing(Artifact::getUri));
        linkClone.getProducts().sort(Comparator.comparing(Artifact::getUri));
        return serializeSignable(linkClone);
    }

    @Override
    public String serialize(Layout layout) {
        Layout layoutClone = Mappers.getMapper(Cloner.class).clone(layout);
        layoutClone.getSteps().sort(Comparator.comparing(Step::getStepName));
        return serializeSignable(layoutClone);
    }

    private String serializeSignable(Object signable) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        try {
            return objectMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
}
