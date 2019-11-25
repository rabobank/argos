package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@Builder
public class LayoutMetaBlock {
    private String supplyChainId;

    @Builder.Default
    private String layoutMetaBlockId = randomUUID().toString();

    private List<Signature> signatures;

    private Layout layout;
}
