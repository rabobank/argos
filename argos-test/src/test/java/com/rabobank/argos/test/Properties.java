package com.rabobank.argos.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

@Getter
public class Properties {

    private final String apiBaseUr;
    private static Properties INSTANCE;

    public static Properties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Properties();
        }
        return INSTANCE;
    }

    private Properties() {
        Config conf = ConfigFactory.load();
        apiBaseUr = conf.getString("argos-service.rest-api.base-url");

    }
}

