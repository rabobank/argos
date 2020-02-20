package com.rabobank.argos.service.domain.security;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LabelCheckData {
    private String labelId;
    private String parentLabelId;
}
