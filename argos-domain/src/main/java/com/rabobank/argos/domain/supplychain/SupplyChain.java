package com.rabobank.argos.domain.supplychain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@Builder
public class SupplyChain {

    @Builder.Default
    private String supplyChainId = randomUUID().toString();

    private String name;
}
