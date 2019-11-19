package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class LayoutMetaBlock {
    private String id;
    private Set<Signature> signatures;
    private ZonedDateTime expires;
    private Layout layout;

    public boolean isExpired() {
        return !(this.expires == null || this.expires.compareTo(ZonedDateTime.now(ZoneId.of("GMT"))) >= 0);
    }
}
