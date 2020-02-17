package com.rabobank.argos.service.security;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.service.domain.account.NonPersonalAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonPersonalAccountUserDetailsServiceTest {
    private static final String USER_NAME = "test";
    private static final String KEY_ID = "keyId";
    @Mock
    private NonPersonalAccountRepository nonPersonalAccountRepository;
    private NonPersonalAccount nonPersonalAccount = NonPersonalAccount.builder().name(USER_NAME).build();
    private NonPersonalAccountUserDetailsService nonPersonalAccountUserDetailsService;

    @BeforeEach
    void setup() {
        nonPersonalAccountUserDetailsService = new NonPersonalAccountUserDetailsService(nonPersonalAccountRepository);
    }

    @Test
    void loadUserByIdWithValidIdShouldReturnUserdetails() {
        when(nonPersonalAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.of(nonPersonalAccount));
        UserDetails userDetails = nonPersonalAccountUserDetailsService.loadUserById(KEY_ID);
        assertThat(userDetails.getUsername(), is(USER_NAME));
    }

    @Test
    void loadUserByIdWithInValidIdShouldReturnError() {
        when(nonPersonalAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.empty());
        Exception exception = assertThrows(ArgosError.class, () -> nonPersonalAccountUserDetailsService.loadUserById("keyId"));
        assertThat(exception.getMessage(), is("Non personal account with keyid keyId not found"));
    }

}