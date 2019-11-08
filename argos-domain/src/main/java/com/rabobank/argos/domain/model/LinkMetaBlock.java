package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LinkMetaBlock {
    private String supplyChainId;
    private Signature signature;
    private Link link;
}
