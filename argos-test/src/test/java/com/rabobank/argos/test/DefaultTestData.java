package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class DefaultTestData {
    private String adminToken;
    private Map<String, PersonalAccount> personalAccounts = new HashMap<>();
    private RestLabel defaultRootLabel;
    private Map<String, NonPersonalAccount> nonPersonalAccount = new HashMap<>();

    @Builder
    @Getter
    @Setter
    public static class PersonalAccount {
        String token;
        String keyId;
        String passphrase;
    }

    @Builder
    @Getter
    @Setter
    public static class NonPersonalAccount {
        String keyId;
        String passphrase;
        String hashedKeyPassphrase;
    }

}

