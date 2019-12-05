package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchFilterReadConverterTest {

    public static final String STEP_NAME = "stepName";
    public static final String ARTIFACT_JAVA = "/artifact.java";
    public static final String PRODUCTS = "PRODUCTS";
    @Mock
    private DBObject dbObject;
    private MatchFilterReadConverter matchFilterReadConverter;

    @BeforeEach
    public void setup() {
        when(dbObject.get(eq("destinationStepName"))).thenReturn(STEP_NAME);
        when(dbObject.get(eq("pattern"))).thenReturn(ARTIFACT_JAVA);
        when(dbObject.get(eq("destinationType"))).thenReturn(PRODUCTS);
        matchFilterReadConverter = new MatchFilterReadConverter();

    }

    @Test
    void testConvert() {
        MatchFilter matchFilter = matchFilterReadConverter.convert(dbObject);
        assertThat(matchFilter.getDestinationType(), is(DestinationType.PRODUCTS));
        assertThat(matchFilter.getPattern(), is(ARTIFACT_JAVA));
        assertThat(matchFilter.getDestinationStepName(), is(STEP_NAME));
    }

}
