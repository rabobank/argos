package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    @Override
    public ResponseEntity<Void> createLLayout(String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        return null;
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> getLayout(String supplyChainId, String layoutId) {
        return null;
    }

    @Override
    public ResponseEntity<List<RestLayoutMetaBlock>> findLayout(String supplyChainId) {
        return null;
    }
}
