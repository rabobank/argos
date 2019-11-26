package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.link.Link;

public interface SigningSerializer {

    String serialize(Link link);

    String serialize(Layout layout);
}
