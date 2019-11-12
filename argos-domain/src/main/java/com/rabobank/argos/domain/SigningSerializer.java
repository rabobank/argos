package com.rabobank.argos.domain;

import com.rabobank.argos.domain.model.Link;

public interface SigningSerializer {
    String serialize(Link link);
}
