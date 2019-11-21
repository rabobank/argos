package com.rabobank.argos.service.adapter.out.mongodb.config;

import com.rabobank.argos.service.adapter.out.mongodb.converter.ByteArrayToPublicKeyToReadConverter;
import com.rabobank.argos.service.adapter.out.mongodb.converter.PublicKeyToByteArrayWriteConverter;
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
