package com.rabobank.argos.service.adapter.out.mongodb;

import com.rabobank.argos.service.adapter.out.mongodb.key.converter.ByteArrayToPublicKeyToReadConverter;
import com.rabobank.argos.service.adapter.out.mongodb.key.converter.PublicKeyToByteArrayWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new ByteArrayToPublicKeyToReadConverter());
        converterList.add(new PublicKeyToByteArrayWriteConverter());
        return new MongoCustomConversions(converterList);
    }
}
