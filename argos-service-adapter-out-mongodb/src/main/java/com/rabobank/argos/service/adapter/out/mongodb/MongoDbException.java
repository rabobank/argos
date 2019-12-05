package com.rabobank.argos.service.adapter.out.mongodb;

public class MongoDbException extends RuntimeException {

    public MongoDbException(Exception e) {
        super(e);
    }
}
