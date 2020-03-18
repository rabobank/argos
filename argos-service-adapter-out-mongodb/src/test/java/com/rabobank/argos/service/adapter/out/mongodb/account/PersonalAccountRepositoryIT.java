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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.AccountSearchParams;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonalAccountRepositoryIT {

    public static final PersonalAccount PIETJE = PersonalAccount.builder().name("Pietje").email("pietje@piet.nl").build();
    public static final PersonalAccount KLAASJE = PersonalAccount.builder().name("Klaasje").email("klaasje@klaas.nl").build();
    private MongodExecutable mongodExecutable;
    private PersonalAccountRepository personalAccountRepository;

    @BeforeAll
    void setup() throws IOException, MongobeeException {
        String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        String connectionString = "mongodb://localhost:" + port;
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        personalAccountRepository = new PersonalAccountRepositoryImpl(mongoTemplate);

        Mongobee runner = new Mongobee(connectionString);
        runner.setChangeLogsScanPackage("com.rabobank.argos.service.adapter.out.mongodb.account");
        runner.setMongoTemplate(mongoTemplate);
        runner.setDbName("test");
        runner.execute();
        loadData();
    }

    private void loadData() {
        personalAccountRepository.save(PIETJE);
        personalAccountRepository.save(KLAASJE);
    }

    @Test
    void searchByName() {
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("tje").build()), contains(PIETJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("je").build()), contains(KLAASJE, PIETJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("J").build()), contains(KLAASJE, PIETJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("Klaa").build()), contains(KLAASJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("klaasje").build()), contains(KLAASJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("Pietje").build()), contains(PIETJE));
        assertThat(personalAccountRepository.search(AccountSearchParams.builder().name("z").build()), empty());
    }

    @AfterAll
    void clean() {
        mongodExecutable.stop();
    }
}
