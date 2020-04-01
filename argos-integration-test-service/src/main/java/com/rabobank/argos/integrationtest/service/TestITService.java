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

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.account.AuthenticationProvider;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.JsonSigningSerializer;
import com.rabobank.argos.integrationtest.argos.service.api.handler.IntegrationTestServiceApi;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestKeyPair;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestLinkMetaBlock;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestPersonalAccount;
import com.rabobank.argos.integrationtest.argos.service.api.model.RestPersonalAccountWithToken;
import com.rabobank.argos.integrationtest.service.layout.LayoutMetaBlockMapper;
import com.rabobank.argos.integrationtest.service.link.LinkMetaBlockMapper;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequestMapping("/integration-test")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestITService implements IntegrationTestServiceApi {

    @Value("${jwt.token.secret}")
    private String secret;

    public static final String PBE_WITH_SHA_1_AND_DE_SEDE = "PBEWithSHA1AndDESede";
    private final RepositoryResetProvider repositoryResetProvider;

    private final LayoutMetaBlockMapper layoutMetaBlockMapper;

    private final LinkMetaBlockMapper linkMetaBlockMapper;

    private final AccountService accountService;

    private final PersonalAccountRepository personalAccountRepository;

    private SecretKey secretKey;

    private final AccountMapper accountMapper;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    @Override
    public ResponseEntity<Void> resetDatabase() {
        log.info("resetDatabase");
        repositoryResetProvider.resetNotAllRepositories();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> resetDatabaseAll() {
        log.info("resetDatabaseAll");
        repositoryResetProvider.resetAllRepositories();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RestKeyPair> createKeyPair(String password) {
        KeyPair keyPair = generateKeyPair();
        String keyId = KeyIdProvider.computeKeyId(keyPair.getPublic());
        byte[] privateKey = addPassword(keyPair.getPrivate().getEncoded(), password);
        return ResponseEntity.ok(new RestKeyPair().keyId(keyId).encryptedPrivateKey(privateKey).publicKey(keyPair.getPublic().getEncoded()));
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> signLayout(String password, String keyId, RestLayoutMetaBlock restLayoutMetaBlock) {
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockMapper.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        String signature = createSignature(getPrivateKey(password, keyId), new JsonSigningSerializer().serialize(layoutMetaBlock.getLayout()));

        List<Signature> signatures = new ArrayList<>(layoutMetaBlock.getSignatures());
        signatures.add(Signature.builder().signature(signature).keyId(keyId).build());
        layoutMetaBlock.setSignatures(signatures);
        return ResponseEntity.ok(layoutMetaBlockMapper.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    public ResponseEntity<RestLinkMetaBlock> signLink(String password, String keyId, RestLinkMetaBlock restLinkMetaBlock) {
        LinkMetaBlock linkMetaBlock = linkMetaBlockMapper.convertFromRestLinkMetaBlock(restLinkMetaBlock);

        String signature = createSignature(getPrivateKey(password, keyId), new JsonSigningSerializer().serialize(linkMetaBlock.getLink()));
        linkMetaBlock.setSignature(Signature.builder().signature(signature).keyId(keyId).build());
        return ResponseEntity.ok(linkMetaBlockMapper.convertToRestLinkMetaBlock(linkMetaBlock));

    }

    @Override
    public ResponseEntity<RestPersonalAccountWithToken> createPersonalAccount(RestPersonalAccount restPersonalAccount) {
        PersonalAccount personalAccount = PersonalAccount.builder()
                .name(restPersonalAccount.getName())
                .email(restPersonalAccount.getEmail())
                .provider(AuthenticationProvider.AZURE)
                .providerId(UUID.randomUUID().toString())
                .roleIds(Collections.emptyList())
                .build();

        personalAccountRepository.save(personalAccount);

        RestPersonalAccountWithToken restPersonalAccountWithToken = accountMapper.map(personalAccount);
        restPersonalAccountWithToken.setToken(createToken(restPersonalAccountWithToken.getId()));
        return ResponseEntity.ok(restPersonalAccountWithToken);
    }

    @Override
    public ResponseEntity<Void> deletePersonalAccount(String accountId) {
        repositoryResetProvider.deletePersonalAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    public String createToken(String accountId) {
        return Jwts.builder()
                .setSubject(accountId)
                .setIssuedAt(new Date())
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plus(Period.ofDays(1))))
                .signWith(secretKey)
                .compact();
    }

    private PrivateKey getPrivateKey(String password, String keyId) {
        return decryptPrivateKey(accountService.findKeyPairByKeyId(keyId).orElseThrow().getEncryptedPrivateKey(), password.toCharArray());
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private byte[] addPassword(byte[] encodedprivkey, String password) {
        // extract the encoded private key, this is an unencrypted PKCS#8 private key

        try {
            int count = 20;// hash iteration count
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            // Create PBE parameter set
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);

            // Initialize PBE Cipher with key and parameters
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

            // Encrypt the encoded Private Key with the PBE key
            byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);

            // Now construct  PKCS #8 EncryptedPrivateKeyInfo object
            AlgorithmParameters algparms = AlgorithmParameters.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);
            algparms.init(pbeParamSpec);
            EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);


            // and here we have it! a DER encoded PKCS#8 encrypted key!
            return encinfo.getEncoded();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey decryptPrivateKey(byte[] encodedPrivateKey, char[] keyPassphrase) {
        try {
            PKCS8EncryptedPrivateKeyInfo encPKInfo = new PKCS8EncryptedPrivateKeyInfo(encodedPrivateKey);
            log.info("EncryptionAlgorithm : {}", encPKInfo.getEncryptionAlgorithm().getAlgorithm());
            InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(keyPassphrase);
            PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
            return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private String createSignature(PrivateKey privateKey, String jsonRepr) {
        try {
            java.security.Signature privateSignature = java.security.Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(privateSignature.sign());
        } catch (GeneralSecurityException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
