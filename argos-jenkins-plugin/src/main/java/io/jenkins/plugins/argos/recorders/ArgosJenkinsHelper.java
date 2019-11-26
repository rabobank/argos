package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import hudson.security.ACL;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Collections;

@AllArgsConstructor
@Slf4j
public class ArgosJenkinsHelper {

    private final String privateKeyCredentialId;
    private final String stepName;
    private final String supplyChainName;
    private final String runId;


    public Argos4j createArgos() {

        checkProperty(privateKeyCredentialId, "privateKeyCredentialId");
        checkProperty(stepName, "stepName");
        checkProperty(supplyChainName, "supplyChainName");
        checkProperty(runId, "runId");


        String argosServiceBaseUrl = ArgosServiceConfiguration.get().getArgosServiceBaseUrl()+"/api";
        checkProperty(argosServiceBaseUrl, "argosServiceBaseUrl");
        log.info("argosServiceBaseUrl = {}",argosServiceBaseUrl);

        return new Argos4j(Argos4jSettings.builder()
                .stepName(stepName)
                .runId(runId)
                .argosServerBaseUrl(argosServiceBaseUrl)
                .signingKey(getSigningKey(privateKeyCredentialId))
                .supplyChainName(supplyChainName).build());
    }

    private void checkProperty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new Argos4jError(fieldName + " not configured");
        }
    }

    private SigningKey getSigningKey(String privateKeyCredentialId) {
        try {
            return SigningKey.builder().keyPair(getPemKeyPair(getCredentials(privateKeyCredentialId).getContent())).build();
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }

    private static KeyPair getPemKeyPair(InputStream signingKey) {
        try (Reader reader = new InputStreamReader(signingKey, StandardCharsets.UTF_8);
             PEMParser pemReader = new PEMParser(reader)) {
            Object pem = pemReader.readObject();
            PEMKeyPair kpr;
            if (pem instanceof PEMKeyPair) {
                kpr = (PEMKeyPair) pem;
            } else if (pem instanceof SubjectPublicKeyInfo) {
                kpr = new PEMKeyPair((SubjectPublicKeyInfo) pem, null);
            } else {
                throw new Argos4jError("Couldn't parse PEM object: " + pem.toString());
            }
            return new JcaPEMKeyConverter().getKeyPair(kpr);
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }

    private FileCredentials getCredentials(String privateKeyCredentialId) {
        FileCredentials fileCredential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        FileCredentials.class,
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
