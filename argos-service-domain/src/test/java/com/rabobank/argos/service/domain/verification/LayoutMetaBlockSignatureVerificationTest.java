package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutMetaBlockSignatureVerificationTest {

    private static final String KEY_ID = "keyId";
    private static final String SIG = "sig";
    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private KeyPairRepository keyPairRepository;

    @Mock
    private VerificationContext context;

    private LayoutMetaBlockSignatureVerification verification;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Signature signature;

    @Mock
    private KeyPair keyPair;

    @Mock
    private PublicKey publicKey;

    @Mock
    private Layout layout;

    @BeforeEach
    void setUp() {
        verification = new LayoutMetaBlockSignatureVerification(signatureValidator, keyPairRepository);
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE));
    }

    @Test
    void verifyOkay() {
        mockSetup(true);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
    }

    @Test
    void verifyNotOkay() {
        mockSetup(false);
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }

    private void mockSetup(boolean valid) {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(signatureValidator.isValid(layout, SIG, publicKey)).thenReturn(valid);
        when(signature.getSignature()).thenReturn(SIG);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getSignatures()).thenReturn(Collections.singletonList(signature));
    }

    @Test
    void verifyKeyNotFound() {

        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.empty());
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getSignatures()).thenReturn(Collections.singletonList(signature));
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }
}
