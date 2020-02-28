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
package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.security.ACL;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class ArgosJenkinsHelper {

    private static final Pattern SUPPLY_CHAIN_PATH_REGEX = Pattern.compile("([\\w.]+):([\\w\\-]+)");

    private final String privateKeyCredentialId;
    private final String layoutSegmentName;
    private final String stepName;
    private final String supplyChainIdentifier;
    private final String runId;

    public Argos4j createArgos() {

        String version = Optional.ofNullable(Jenkins.getInstanceOrNull())
                .flatMap(jenkins -> Optional.ofNullable(jenkins.getPlugin("bouncycastle-api")))
                .map(Plugin::getWrapper)
                .map(PluginWrapper::getVersion).orElseThrow(() -> new Argos4jError("bouncycastle-api plugin not installed"));

        if (Float.parseFloat(version) < 1.8F) {
            throw new Argos4jError("bouncycastle-api plugin " + version + " installed, minimal version 1.8 required");
        }

        checkProperty(privateKeyCredentialId, "privateKeyCredentialId");
        checkProperty(layoutSegmentName, "layoutSegmentName");
        checkProperty(stepName, "stepName");
        checkProperty(supplyChainIdentifier, "supplyChainIdentifier");
        checkProperty(runId, "runId");


        String argosServiceBaseUrl = ArgosServiceConfiguration.get().getArgosServiceBaseUrl() + "/api";
        checkProperty(argosServiceBaseUrl, "argosServiceBaseUrl");
        log.info("argos4j version = {}", Argos4j.getVersion());
        log.info("argosServiceBaseUrl = {}", argosServiceBaseUrl);

        String supplyChainName = getSupplyChainName(supplyChainIdentifier);
        List<String> pathToRoot = getPathToRoot(supplyChainIdentifier);

        return new Argos4j(Argos4jSettings.builder()
                .pathToLabelRoot(pathToRoot)
                .layoutSegmentName(layoutSegmentName)
                .stepName(stepName)
                .runId(runId)
                .argosServerBaseUrl(argosServiceBaseUrl)
                .signingKeyId(getCredentials(privateKeyCredentialId).getUsername())
                .supplyChainName(supplyChainName).build());
    }

    private String getSupplyChainName(String supplyChainPath) {
        Matcher matcher = SUPPLY_CHAIN_PATH_REGEX.matcher(supplyChainPath);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            throw new Argos4jError(supplyChainPath + " not correct should be <label>.<label>:<supplyChainName>");
        }
    }

    private List<String> getPathToRoot(String supplyChainPath) {
        Matcher matcher = SUPPLY_CHAIN_PATH_REGEX.matcher(supplyChainPath);
        if (matcher.matches()) {
            List<String> path = new ArrayList<>(Arrays.asList(StringUtils.split(matcher.group(1), '.')));
            Collections.reverse(path);
            return path;
        } else {
            throw new Argos4jError(supplyChainPath + " not correct should be <label>.<label>:<supplyChainName>");
        }
    }

    public static char[] getPrivateKeyPassword(String privateKeyCredentialId) {
        return getCredentials(privateKeyCredentialId).getPassword().getPlainText().toCharArray();
    }

    private void checkProperty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new Argos4jError(fieldName + " not configured");
        }
    }

    private static StandardUsernamePasswordCredentials getCredentials(String privateKeyCredentialId) {
        StandardUsernamePasswordCredentials fileCredential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()
                ),
                CredentialsMatchers.withId(privateKeyCredentialId)
        );

        if (fileCredential == null)
            throw new Argos4jError(" Could not find credentials entry with ID '" + privateKeyCredentialId + "' ");

        return fileCredential;
    }

}
