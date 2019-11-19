package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class LayoutMetaBlock {
    private String id;
    private Set<Signature> signatures;
    private Layout layout;
}
