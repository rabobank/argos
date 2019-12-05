package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class MatchFilterWriteConverterTest {

    private static final String ARTIFACT_JAVA = "/artifact.java";
    private static final String STEP_NAME = "StepName";
    private MatchFilterWriteConverter matchFilterWriteConverter;
    private MatchFilter matchFilter;

    @BeforeEach
    public void setup() {
        matchFilterWriteConverter = new MatchFilterWriteConverter();
        matchFilter = MatchFilter
                .builder()
                .pattern(ARTIFACT_JAVA)
                .destinationStepName(STEP_NAME)
                .destinationType(DestinationType.PRODUCTS)
                .build();

    }

    @Test
    void testConvert() {
        DBObject dbObject = matchFilterWriteConverter.convert(matchFilter);
        assertThat(dbObject.get("pattern"), is(ARTIFACT_JAVA));
        assertThat(dbObject.get("destinationStepName"), is(STEP_NAME));
        assertThat(dbObject.get("destinationType"), is("PRODUCTS"));
    }
}
