package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ByProducts {
    private Integer returnValue;
    private String stderr;
    private String stdout;
}
