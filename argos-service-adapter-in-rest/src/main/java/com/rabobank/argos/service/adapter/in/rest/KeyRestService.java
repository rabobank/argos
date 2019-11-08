package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.argos4j.internal.Argos4JSigner;
import com.rabobank.argos.domain.KeyPairRepository;
import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.handler.KeyApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.mapper.KeyPairMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class KeyRestService implements KeyApi {

    private final KeyPairMapper converter;
    private final KeyPairRepository keyPairRepository;
    private Argos4JSigner argos4JSigner = new Argos4JSigner();

    @Override
    public ResponseEntity<RestKeyPair> getKey(String keyId) {
        KeyPair keyPair = keyPairRepository.findByKeyId(keyId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "key pair not found : " + keyId)
        );
        return new ResponseEntity<>(converter.convertToRestKeyPair(keyPair), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> storeKey(@Valid RestKeyPair restKeyPair) {
        validateKeyId(restKeyPair);
        keyPairRepository.save(converter.convertFromRestKeyPair(restKeyPair));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private void validateKeyId(RestKeyPair keyPair) {
        if(!keyPair.getKeyId().equals(argos4JSigner.computeKeyId(keyPair.getPublicKey()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }
}
