package com.rabobank.argos.service.adapter.out.mongodb.layout.converter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rabobank.argos.domain.layout.MatchFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class MatchFilterWriteConverter implements Converter<MatchFilter, DBObject> {

    @Override
    public DBObject convert(MatchFilter matchFilter) {
        DBObject dbo = new BasicDBObject();
        dbo.put("pattern", matchFilter.getPattern());
        dbo.put("destinationStepName", matchFilter.getDestinationStepName());
        dbo.put("destinationType", matchFilter.getDestinationType().name());
        return dbo;
    }
}
