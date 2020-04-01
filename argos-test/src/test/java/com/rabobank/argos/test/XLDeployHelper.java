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
package com.rabobank.argos.test;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XLDeployHelper {
    

    private static Properties properties = Properties.getInstance();
    
    public static void waitForXLDeployToStart() {
        log.info("Waiting for xldeploy start");
        HttpClient client = HttpClient.newBuilder().authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getXlDeployUser(),
                        properties.getXlDeployPassword().toCharArray());
            }
        }).build();
        await().atMost(1, MINUTES).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getXlDeployBaseUrl()+"/server/state"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });
        log.info("xldeploy started");
    }
    
    public static void initXLDeploy() {
        log.info("Initializing xldeploy");
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(XLDeployHelper.class.getResourceAsStream("/xldeploy/init-cis.json"), writer);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        String json = writer.toString();
        
        HttpClient client = HttpClient.newBuilder().authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getXlDeployUser(),
                        properties.getXlDeployPassword().toCharArray());
            }
        }).build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getXlDeployBaseUrl() + "/repository/cis"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            // ignore
        }
        log.info("xldeploy initialized");
    }

}
