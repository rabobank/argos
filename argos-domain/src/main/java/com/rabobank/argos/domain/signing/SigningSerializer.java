package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;

public interface SigningSerializer {

    String serialize(Link link);

    String serialize(Layout layout);
}
