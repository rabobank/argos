package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.service.adapter.in.rest.api.handler.VerificationApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class VerificationRestService implements VerificationApi {

    private final VerificationProvider verificationProvider;

    private final LayoutMetaBlockRepository repository;

    private final ArtifactMapper artifactMapper;

    private final VerificationResultMapper verificationResultMapper;

    @Override
    public ResponseEntity<RestVerificationResult> performVerification(String supplyChainId, @Valid RestVerifyCommand restVerifyCommand) {

        List<LayoutMetaBlock> layoutMetaBlocks = repository.findBySupplyChainId(supplyChainId);
        if (layoutMetaBlocks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active layout could be found for supplychain:" + supplyChainId);
        }
        List<Artifact> expectedProducts = artifactMapper.mapToArtifacts(restVerifyCommand.getExpectedProducts());
        VerificationProvider.VerificationRunResult verificationRunResult = verificationProvider.verifyRun(layoutMetaBlocks.iterator().next(), expectedProducts);
        return ResponseEntity.ok(verificationResultMapper.mapToRestVerificationResult(verificationRunResult));
    }
}
