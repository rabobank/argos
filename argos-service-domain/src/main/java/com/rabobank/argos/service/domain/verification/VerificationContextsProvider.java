package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.List;

public interface VerificationContextsProvider {
    List<VerificationContext> calculatePossibleVerificationContexts(List<LinkMetaBlock> linkMetaBlocks, LayoutSegment segment, LayoutMetaBlock layoutMetaBlock);
}
