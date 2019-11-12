package com.rabobank.argos.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Link;

import java.util.Comparator;

public class JsonSigningSerializer implements SigningSerializer {


    @Override
    public String serialize(Link link) {

        link.getMaterials().sort(Comparator.comparing(Artifact::getUri));
        link.getProducts().sort(Comparator.comparing(Artifact::getUri));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        try {
            return objectMapper.writeValueAsString(link);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
