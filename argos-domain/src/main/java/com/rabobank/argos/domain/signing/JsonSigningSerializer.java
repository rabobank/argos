package com.rabobank.argos.domain.signing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
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
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Rule.class, new RuleSerializer());
        objectMapper.registerModule(module);
        try {
            return objectMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private static class RuleSerializer extends StdSerializer<Rule> {

        private RuleSerializer() {
            super((Class<Rule>) null);
        }

        @Override
        public void serialize(Rule rule, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            if (rule instanceof MatchRule) {
                MatchRule matchRule = (MatchRule) rule;
                jgen.writeStringField("pattern", rule.getPattern());
                jgen.writeStringField("destinationPathPrefix", matchRule.getDestinationPathPrefix());
                jgen.writeStringField("destinationStepName", matchRule.getDestinationStepName());
                jgen.writeStringField("destinationType", matchRule.getDestinationType().name());
                jgen.writeStringField("sourcePathPrefix", matchRule.getSourcePathPrefix());
            } else {
                jgen.writeStringField("pattern", rule.getPattern());
            }
            jgen.writeStringField("type", rule.getClass().getSimpleName().replace("Rule", "").toUpperCase());
            jgen.writeEndObject();
        }
    }
}
