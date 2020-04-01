/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.integrationtest.service;

import com.rabobank.argos.service.adapter.out.mongodb.MongoConfig;
import com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl;
import com.rabobank.argos.service.domain.account.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {MongoConfig.class, TestITService.class, AccountService.class, NonPersonalAccountRepositoryImpl.class, AccountSecurityContextMock.class})
public class ArgosIntegrationtestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArgosIntegrationtestServiceApplication.class, args);
    }
}
