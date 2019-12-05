package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class MatchFilterReadConverter implements Converter<DBObject, MatchFilter> {

    @Override
    public MatchFilter convert(DBObject dbObject) {
        return MatchFilter.builder()
                .destinationStepName((String) dbObject.get("destinationStepName"))
                .pattern((String) dbObject.get("pattern"))
                .destinationType(DestinationType.valueOf((String) dbObject.get("destinationType")))
                .build();

    }
}
