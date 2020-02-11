package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.service.domain.account.Account;

public class AccountSecurityContextImpl implements AccountSecurityContext {
    @Override
    public Account getAuthenticatedAccount() {
        return null;
    }
}
