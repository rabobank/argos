package com.rabobank.argos.domain.link;

import com.rabobank.argos.domain.Signature;
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
